package com.senai.sistema_almoxarifado.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // Quem não estiver logado é mandado para /login pelo Spring Security
    @GetMapping("/")
    public String raiz() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String getHome() {
        return "home";
    }
}