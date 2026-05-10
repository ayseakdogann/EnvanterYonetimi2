package com.koleksiyon.envanter.controller;

import com.koleksiyon.envanter.dto.UserDTO;
import com.koleksiyon.envanter.entity.User;
import com.koleksiyon.envanter.service.ItemService;
import com.koleksiyon.envanter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ItemService itemService;

    // Aktif oturum açmış kullanıcıyı bulur
    private User getLoggedInUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findByUsername(username);
    }

    // topluluk sayfası
    @GetMapping("/users")
    public String listAllUsers(Model model) {
        // Sadece normal kullanıcıları (ROLE_USER) filtrele, adminleri listeden çıkar
        List<User> normalUsers = userService.getAllUsers().stream()
                .filter(u -> !u.getRole().equals("ROLE_ADMIN"))
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("users", normalUsers);
        model.addAttribute("currentUser", getLoggedInUser());
        model.addAttribute("title", "Koleksiyoncular Topluluğu");
        return "users";
    }

    // profil sayfası
    @GetMapping("/profile/{username}")
    @Transactional(readOnly = true)
    public String showProfile(@PathVariable String username, Model model) {
        User targetUser = userService.findByUsername(username);
        User currentUser = getLoggedInUser();

        if (targetUser == null) return "redirect:/items";

        // Adminin profili yoktur. Biri girmeye çalışırsa pazara geri yolla.
        // Eğer girmeye çalışan kişi adminin kendisiyse, onu dashboard'una yolla.
        if (targetUser.getRole().equals("ROLE_ADMIN")) {
            if (currentUser.getRole().equals("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/items";
        }

        model.addAttribute("user", targetUser);
        model.addAttribute("items", itemService.getItemsByOwner(targetUser));
        model.addAttribute("isFollowing", currentUser.getFollowing().contains(targetUser));
        model.addAttribute("currentUser", currentUser);

        return "profile";
    }

    // takipçileri görüntüleme
    @GetMapping("/profile/{username}/followers")
    @Transactional(readOnly = true)
    public String showFollowers(@PathVariable String username, Model model) {
        User targetUser = userService.findByUsername(username);
        if (targetUser == null) return "redirect:/items";

        model.addAttribute("users", targetUser.getFollowers());
        model.addAttribute("currentUser", getLoggedInUser());
        model.addAttribute("title", username + " - Takipçiler");
        return "users";
    }

    // Takip edilenleri görüntüleme
    @GetMapping("/profile/{username}/following")
    @Transactional(readOnly = true)
    public String showFollowing(@PathVariable String username, Model model) {
        User targetUser = userService.findByUsername(username);
        if (targetUser == null) return "redirect:/items";

        model.addAttribute("users", targetUser.getFollowing());
        model.addAttribute("currentUser", getLoggedInUser());
        model.addAttribute("title", username + " - Takip Edilenler");
        return "users";
    }

    // profil düzenleme formu
    @GetMapping("/profile/edit")
    public String editProfileForm(Model model) {
        User currentUser = getLoggedInUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("userDTO", new UserDTO());
        return "profile-edit";
    }

    // profil güncelleme
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UserDTO userDTO) throws IOException {
        User currentUser = getLoggedInUser();

        currentUser.setBio(userDTO.getBio());
        currentUser.setCity(userDTO.getCity());
        currentUser.setBirthDate(userDTO.getBirthDate());

        if (userDTO.getProfileImageFile() != null && !userDTO.getProfileImageFile().isEmpty()) {
            currentUser.setProfileImage(userDTO.getProfileImageFile().getBytes());
        }

        userService.updateUser(currentUser);
        return "redirect:/profile/" + currentUser.getUsername();
    }

    // profil resmini çeken endpoint
    @GetMapping("/profile/image/{username}")
    @ResponseBody
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user != null && user.getProfileImage() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(user.getProfileImage());
        }
        return ResponseEntity.notFound().build();
    }

    // takip etme takipten çıkma mantığı
    @PostMapping("/follow/{username}")
    @Transactional
    public String followUser(@PathVariable String username) {
        User currentUser = getLoggedInUser();
        User targetUser = userService.findByUsername(username);

        if (targetUser != null && !currentUser.equals(targetUser)) {
            if (currentUser.getFollowing().contains(targetUser)) {
                currentUser.getFollowing().remove(targetUser);
            } else {
                currentUser.getFollowing().add(targetUser);
            }
            userService.updateUser(currentUser);
        }
        return "redirect:/profile/" + username;
    }

    // kendi koleksiyonum
    @GetMapping("/my-collection")
    public String myCollection(Model model) {
        User currentUser = getLoggedInUser();

        // Admin koleksiyon sayfasına girmeye çalışırsa kendi paneline yolla
        if (currentUser.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("items", itemService.getItemsByOwner(currentUser));
        model.addAttribute("currentUser", currentUser);
        return "my-collection";
    }
}