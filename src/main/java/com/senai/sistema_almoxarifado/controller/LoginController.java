package com.senai.sistema_almoxarifado.controller;

import com.senai.sistema_almoxarifado.dto.LoginDto;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.exceptions.LoginInvalidoException;
import com.senai.sistema_almoxarifado.service.UsuarioService;
import com.senai.sistema_almoxarifado.sessoes.SessaoDto;
import com.senai.sistema_almoxarifado.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public String fazerLogin(@Valid @ModelAttribute("login") LoginDto loginDto,
                             BindingResult bindingResult,
                             HttpSession session, Model model){

        if(bindingResult.hasErrors()){
            model.addAttribute("erroLogin", "Preencha login e senha.");
            return "login";
        }

        try {
            UsuarioEntity login = usuarioService.validarLogin(loginDto);

            SessaoDto sessaoDto = new SessaoDto(login.getId(), login.getNome());

            SessaoUtil.registrarSessao(session, sessaoDto);

            return "redirect:/home";

        } catch (LoginInvalidoException e) {
            model.addAttribute("erroLogin",e.getMessage());
           return "login";
        }
    }

    @GetMapping("/logout")
    public String fazerLogout(HttpSession session) {
        SessaoUtil.removerSessao(session);
        return "redirect:/login";
    }
}
