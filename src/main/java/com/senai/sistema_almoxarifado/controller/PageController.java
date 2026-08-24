package com.senai.sistema_almoxarifado.controller;

import com.senai.sistema_almoxarifado.dto.LoginDto;
import com.senai.sistema_almoxarifado.service.UsuarioService;
import com.senai.sistema_almoxarifado.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UsuarioService usuarioService;

    @GetMapping("/")
    public String raiz(HttpSession session) {
        if (SessaoUtil.obterSessao(session) != null) {
            return "redirect:/home";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLogin(@ModelAttribute("login") LoginDto loginDto,
                           Model model) {
        model.addAttribute("login", loginDto);
        return "login";
    }

    @GetMapping("/home")
    public String getHome(HttpSession session){
        if (SessaoUtil.obterSessao(session) == null) {
            return "redirect:/login";
        }

        return "home";
    }
}
