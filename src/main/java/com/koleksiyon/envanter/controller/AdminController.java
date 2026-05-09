package com.koleksiyon.envanter.controller;

import com.koleksiyon.envanter.entity.SystemVault;
import com.koleksiyon.envanter.repository.SystemVaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SystemVaultRepository systemVaultRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        SystemVault vault = systemVaultRepository.findById(1L).orElse(new SystemVault());
        model.addAttribute("totalCommission", vault.getTotalCommission());
        return "admin-dashboard";
    }
}