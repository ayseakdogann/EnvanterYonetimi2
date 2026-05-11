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
    public String registerUser(@ModelAttribute("user") User user) { //HTML formunda kullanıcının girdiği ad, e-posta, şifre gibi bilgileri alır ve otomatik olarak bizim Java'daki User nesnemizin içine doldurur
        userService.saveUser(user); //İçi dolan bu kullanıcı nesnesini alır ve veritabanına kaydetmesi için UserService'e yollar.
        return "redirect:/login?registered=true";
    }
}