package com.senai.sistema_almoxarifado.config;

import com.senai.sistema_almoxarifado.entity.Papel;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetupInicial implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByLogin("admin").isEmpty()) {
            usuarioRepository.save(new UsuarioEntity(null, "Administrador do Sistema", "admin",
                    passwordEncoder.encode("123456"), Papel.ROLE_ADMIN));
        }
        if (usuarioRepository.findByLogin("usuario").isEmpty()) {
            usuarioRepository.save(new UsuarioEntity(null, "Colaborador Padrão", "usuario",
                    passwordEncoder.encode("123456"), Papel.ROLE_USER));
        }

        // Usuários antigos com senha em texto puro: converte para BCrypt
        usuarioRepository.findAll().forEach(u -> {
            if (!u.getSenha().startsWith("$2")) {
                u.setSenha(passwordEncoder.encode(u.getSenha()));
                usuarioRepository.save(u);
            }
        });
    }
}