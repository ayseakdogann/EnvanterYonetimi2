package com.koleksiyon.envanter.controller;

import com.koleksiyon.envanter.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private com.koleksiyon.envanter.service.UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new com.koleksiyon.envanter.entity.User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") com.koleksiyon.envanter.entity.User user,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        String password = user.getPassword();

        // Şifre 6 karakterden kısa mı VEYA sadece rakamlardan oluşmuyor mu?
        if (password.length() < 6 || !password.matches("\\d+")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Kayıt başarısız: Şifreniz en az 6 haneli olmalı ve sadece rakamlardan oluşmalıdır!");
            return "redirect:/register"; // Hata varsa kayıt sayfasına geri yolla
        }

        userService.saveUser(user);
        return "redirect:/login?registered=true";
    }
}