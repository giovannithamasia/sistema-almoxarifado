package com.senai.sistema_almoxarifado.service;

import com.senai.sistema_almoxarifado.dto.LoginDto;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.exceptions.LoginInvalidoException;
import com.senai.sistema_almoxarifado.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioEntity validarLogin(LoginDto dto) {
        return usuarioRepository.findByLoginAndSenha(dto.login(),dto.senha())
                .orElseThrow(() -> new LoginInvalidoException("Login ou senha inválidos."));
    }
}
