package com.senai.sistema_almoxarifado.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/acesso-negado")
    public String acessoNegado() { return "acesso-negado"; }
}