package com.senai.sistema_almoxarifado.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Público: arquivos estáticos, login e página de erro
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/error").permitAll()

                        // Só ADMIN: cadastrar (GET e POST) e excluir produto
                        .requestMatchers("/produtocadastrar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/produtoexcluir/**").hasRole("ADMIN")

                        // ADMIN e USER: listar e editar produto
                        .requestMatchers("/produtolista", "/produtoatualizar/**").hasAnyRole("ADMIN", "USER")

                        // ADMIN e USER: home e estoque
                        .requestMatchers("/home", "/movimentacoes/**", "/movimentacaocadastrar").hasAnyRole("ADMIN", "USER")

                        // Qualquer outra rota: basta estar logado
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(e -> e.accessDeniedPage("/acesso-negado"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}