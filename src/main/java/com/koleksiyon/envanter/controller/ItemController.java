package com.koleksiyon.envanter.controller;

import com.koleksiyon.envanter.dto.ItemDTO;
import com.koleksiyon.envanter.entity.Item;
import com.koleksiyon.envanter.entity.User;
import com.koleksiyon.envanter.repository.ItemRepository;
import com.koleksiyon.envanter.service.ItemService;
import com.koleksiyon.envanter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;
    private final ItemRepository itemRepository;

    // Giriş yapmış kullanıcıyı getiren yardımcı metot
    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }

    // Sadece satışta olan ürünleri filtreli gösterir
    @GetMapping
    public String listItems(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "sort", required = false) String sort,
            Model model) {

        List<Item> items;

        if (search != null && !search.isEmpty()) {
            items = itemRepository.findByNameContainingIgnoreCase(search);
        } else {
            items = itemService.getItemsForSale(sort, category);
        }

        model.addAttribute("items", items);
        model.addAttribute("currentUser", getLoggedInUser());
        return "index";
    }

    // ürün ekleme formu
    @GetMapping("/add")
    public String showAddForm(Model model) {
        User currentUser = getLoggedInUser();

        // Adminlerin ürün eklemesini engelleme kontrolü
        if (currentUser.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/items?error=admin_cannot_add";
        }

        model.addAttribute("itemDTO", new ItemDTO());
        model.addAttribute("currentUser", currentUser);
        return "item-form";
    }

    // koleksiyona kaydetme
    @Transactional
    @PostMapping("/save")
    public String saveItem(@ModelAttribute("itemDTO") ItemDTO itemDTO) throws IOException {
        if (getLoggedInUser().getRole().equals("ROLE_ADMIN")) {
            return "redirect:/items?error=admin_cannot_add";
        }
        Item item = new Item();
        item.setName(itemDTO.getName());
        item.setType(itemDTO.getType());
        item.setDescription(itemDTO.getDescription());
        item.setStartingPrice(itemDTO.getStartingPrice());

        item.setOwner(getLoggedInUser());
        item.setForSale(false);

        itemService.saveItem(item, itemDTO.getImageFile());
        return "redirect:/my-collection";
    }

    // ürünü satışa çıkarma
    @PostMapping("/list-for-sale/{id}")
    @Transactional
    public String listForSale(@PathVariable Long id) {
        Item item = itemService.getItemById(id);
        User currentUser = getLoggedInUser();

        if (item.getOwner().getUsername().equals(currentUser.getUsername())) {
            item.setForSale(true);
            itemRepository.save(item);
        }
        return "redirect:/items";
    }

    // ürün detay sayfası
    @GetMapping("/{id}")
    public String showItemDetails(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        model.addAttribute("item", item);
        model.addAttribute("currentUser", getLoggedInUser());
        return "item-details";
    }

    // teklif verme
    @Transactional
    @PostMapping("/bid/{id}")
    public String placeBid(@PathVariable Long id, @RequestParam("amount") Double amount, RedirectAttributes redirectAttributes) {
        try {
            itemService.placeBid(id, amount, getLoggedInUser());
            redirectAttributes.addFlashAttribute("successMessage", "Teklifiniz başarıyla verildi!");
        } catch (IllegalArgumentException e) {
            // Servis katmanından gelen "Teklifiniz yetersiz" mesajını yakalayıp sayfaya gönderir
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/items/" + id;
    }

    // ürünü silme
    @RequestMapping(value = "/delete/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @Transactional
    public String deleteItem(@PathVariable Long id) {
        Item item = itemService.getItemById(id);
        User currentUser = getLoggedInUser();

        if (item == null) {
            return "redirect:/my-collection";
        }

        // Yetki Kontrolü
        if (currentUser.getRole().equals("ROLE_ADMIN") ||
                item.getOwner().getUsername().equals(currentUser.getUsername())) {

            itemService.deleteItem(id);

            // Sildikten sonra koleksiyonuma dön
            return "redirect:/my-collection";
        }

        return "redirect:/items?error=unauthorized";
    }

    // resim getirme
    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getItemImage(@PathVariable Long id) {
        Item item = itemService.getItemById(id);
        if (item.getImage() != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(item.getImage());
        }
        return ResponseEntity.notFound().build();
    }

    // Satışı Onaylama ve Parayı Hesaba Aktarma
    @PostMapping("/close-auction/{id}")
    public String closeAuction(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            itemService.closeAuction(id, getLoggedInUser().getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Satış tamamlandı! Komisyon kesildikten sonra kalan tutar cüzdanınıza eklendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/items/" + id;
    }
}