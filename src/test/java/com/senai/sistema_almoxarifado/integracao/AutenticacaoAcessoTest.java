package com.senai.sistema_almoxarifado.integracao;

import com.senai.sistema_almoxarifado.entity.Papel;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AutenticacaoAcessoTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        garantirUsuario("giovanni", "Giovanni", "gio123", Papel.ROLE_ADMIN);
        garantirUsuario("maria", "Maria", "maria123", Papel.ROLE_USER);
    }

    private void garantirUsuario(String login, String nome, String senha, Papel papel) {
        if (usuarioRepository.findByLogin(login).isEmpty()) {
            usuarioRepository.save(new UsuarioEntity(null, nome, login, passwordEncoder.encode(senha), papel));
        }
    }

    @Test
    @DisplayName("CT-01 — Login com credenciais válidas cria a sessão e vai para /home")
    void ct01_loginValido() throws Exception {
        mockMvc.perform(formLogin("/login").user("giovanni").password("gio123"))
                .andExpect(authenticated().withUsername("giovanni"))
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("CT-02 — Login com senha incorreta volta para /login?error, sem sessão")
    void ct02_loginSenhaIncorreta() throws Exception {
        mockMvc.perform(formLogin("/login").user("giovanni").password("SenhaErrada123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @DisplayName("CT-03 (lado servidor) — Login com usuário em branco também é recusado")
    void ct03_loginUsuarioEmBranco() throws Exception {
        mockMvc.perform(formLogin("/login").user("").password("gio123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @DisplayName("CT-04 — Logout encerra a sessão e redireciona para /login?logout")
    void ct04_logout() throws Exception {
        mockMvc.perform(post("/logout").with(user("giovanni").roles("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("CT-05 — Página protegida sem sessão redireciona para o login")
    void ct05_paginaProtegidaSemSessao() throws Exception {
        mockMvc.perform(get("/produtolista"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("CT-05 (variação) — Tela de estoque sem sessão também redireciona para o login")
    void ct05_estoqueSemSessao() throws Exception {
        mockMvc.perform(get("/movimentacoes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("CT-26 — Perfil USER não acessa o cadastro de produto (vai para /acesso-negado)")
    void ct26_userNaoAcessaCadastro() throws Exception {
        mockMvc.perform(get("/produtocadastrar")
                        .with(user("maria").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/acesso-negado"));
    }

    @Test
    @DisplayName("CT-26 (variação) — Perfil USER não pode excluir produto")
    void ct26_userNaoExcluiProduto() throws Exception {
        mockMvc.perform(delete("/produtoexcluir/1")
                        .with(user("maria").roles("USER")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CT-26 (variação) — Perfil USER acessa a lista de produtos e a tela de estoque")
    void ct26_userAcessaListaEEstoque() throws Exception {
        mockMvc.perform(get("/produtolista")
                        .with(user("maria").roles("USER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/movimentacoes")
                        .with(user("maria").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Perfil ADMIN acessa o cadastro de produto")
    void adminAcessaCadastro() throws Exception {
        mockMvc.perform(get("/produtocadastrar")
                        .with(user("giovanni").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
