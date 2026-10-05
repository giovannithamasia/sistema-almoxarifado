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

        // Cadastra o Giovanni (Admin)
        if (usuarioRepository.findByLogin("giovanni").isEmpty()) {
            usuarioRepository.save(new UsuarioEntity(null, "Giovanni", "giovanni",
                    passwordEncoder.encode("gio123"), Papel.ROLE_ADMIN));
        }

        // Cadastra a Maria (User)
        if (usuarioRepository.findByLogin("maria").isEmpty()) {
            usuarioRepository.save(new UsuarioEntity(null, "Maria", "maria",
                    passwordEncoder.encode("maria123"), Papel.ROLE_USER));
        }

        // Cadastra o João (User)
        if (usuarioRepository.findByLogin("joao").isEmpty()) {
            usuarioRepository.save(new UsuarioEntity(null, "João", "joao",
                    passwordEncoder.encode("jo123"), Papel.ROLE_USER));
        }

        // Atualização de segurança: garante que senhas antigas em texto puro sejam criptografadas
        usuarioRepository.findAll().forEach(u -> {
            if (!u.getSenha().startsWith("$2")) {
                u.setSenha(passwordEncoder.encode(u.getSenha()));
                usuarioRepository.save(u);
            }
        });
    }
}