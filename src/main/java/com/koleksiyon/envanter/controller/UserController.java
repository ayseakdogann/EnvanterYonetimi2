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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    // TOPLULUK SAYFASI
    @GetMapping("/users")
    public String listAllUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", getLoggedInUser());
        return "users";
    }

    // PROFİL SAYFASI (Kendisi veya Başkası)
    @GetMapping("/profile/{username}")
    public String showProfile(@PathVariable String username, Model model) {
        User targetUser = userService.findByUsername(username);
        User currentUser = getLoggedInUser();

        if (targetUser == null) return "redirect:/items";

        model.addAttribute("user", targetUser);
        model.addAttribute("items", itemService.getItemsByOwner(targetUser));
        model.addAttribute("isFollowing", currentUser.getFollowing().contains(targetUser));
        model.addAttribute("currentUser", currentUser);

        return "profile";
    }

    // PROFİL DÜZENLEME FORMU
    @GetMapping("/profile/edit")
    public String editProfileForm(Model model) {
        User currentUser = getLoggedInUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("userDTO", new UserDTO());
        return "profile-edit";
    }

    // PROFİL GÜNCELLEME
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

    // PROFİL RESMİNİ ÇEKEN ENDPOINT
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

    // TAKİP ETME / TAKİPTEN ÇIKMA MANTIĞI
    @PostMapping("/follow/{username}")
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

    // KENDİ KOLEKSİYONUM
    @GetMapping("/my-collection")
    public String myCollection(Model model) {
        User currentUser = getLoggedInUser();
        model.addAttribute("items", itemService.getItemsByOwner(currentUser));
        model.addAttribute("currentUser", currentUser);
        return "my-collection";
    }
}