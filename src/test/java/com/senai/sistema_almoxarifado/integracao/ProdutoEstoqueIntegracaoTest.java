package com.senai.sistema_almoxarifado.integracao;

import com.senai.sistema_almoxarifado.dto.produto.ProdutoRespostaDto;
import com.senai.sistema_almoxarifado.entity.MovimentacaoEstoqueEntity;
import com.senai.sistema_almoxarifado.entity.Papel;
import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import com.senai.sistema_almoxarifado.entity.TipoMovimentacaoEstoque;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.repository.MovimentacaoEstoqueRepository;
import com.senai.sistema_almoxarifado.repository.ProdutoRepository;
import com.senai.sistema_almoxarifado.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Testes de integração de produtos e estoque (RF-003 a RF-008): controllers + services + JPA em H2.
 * Cada teste roda dentro de uma transação que é desfeita no final (@Transactional).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProdutoEstoqueIntegracaoTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ProdutoEntity martelo;   // FRM-001 - 50 un., mínimo 10
    private ProdutoEntity chave;     // FRM-002 - 80 un., mínimo 15
    private ProdutoEntity alicate;   // FRM-003 - 30 un., mínimo 5

    private final RequestPostProcessor admin = user("giovanni").roles("ADMIN");
    private final RequestPostProcessor usuarioComum = user("maria").roles("USER");

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        garantirUsuario("giovanni", "Giovanni", "gio123", Papel.ROLE_ADMIN);
        garantirUsuario("maria", "Maria", "maria123", Papel.ROLE_USER);

        martelo = salvarProduto("FRM-001", "Martelo Unha", "Cabo de madeira", 50, 10);
        chave = salvarProduto("FRM-002", "Chave de Fenda", "Ponta chata", 80, 15);
        alicate = salvarProduto("FRM-003", "Alicate Universal", "Cabo isolado", 30, 5);
    }

    private void garantirUsuario(String login, String nome, String senha, Papel papel) {
        if (usuarioRepository.findByLogin(login).isEmpty()) {
            usuarioRepository.save(new UsuarioEntity(null, nome, login, passwordEncoder.encode(senha), papel));
        }
    }

    private ProdutoEntity salvarProduto(String codigo, String nome, String caracteristicas, int atual, int minimo) {
        ProdutoEntity p = new ProdutoEntity();
        p.setCodigo(codigo);
        p.setNome(nome);
        p.setCaracteristicas(caracteristicas);
        p.setEstoqueAtual(atual);
        p.setEstoqueMinimo(minimo);
        return produtoRepository.save(p);
    }

    private ProdutoEntity recarregar(ProdutoEntity p) {
        return produtoRepository.findById(p.getId()).orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> listaDoModelo(MvcResult resultado, String nome) {
        return (List<T>) resultado.getModelAndView().getModel().get(nome);
    }

    // ---------------------------------------------------------------- RF-003 Cadastrar

    @Test
    @DisplayName("CT-06 — Cadastrar produto válido grava no banco e volta para a lista")
    void ct06_cadastrarProdutoValido() throws Exception {
        mockMvc.perform(post("/produtocadastrar").with(admin).with(csrf())
                        .param("codigo", "FRM-004").param("nome", "Trena 5m")
                        .param("caracteristicas", "case plástico, fita de aço")
                        .param("estoqueAtual", "15").param("estoqueMinimo", "10"))
                .andExpect(redirectedUrl("/produtolista"))
                .andExpect(flash().attribute("mensagemCriacao", "Produto cadastrado com sucesso!"));

        assertThat(produtoRepository.existsByCodigo("FRM-004")).isTrue();
    }

    @Test
    @DisplayName("CT-07 — Cadastrar com nome em branco não grava e mantém o formulário")
    void ct07_cadastrarComNomeEmBranco() throws Exception {
        mockMvc.perform(post("/produtocadastrar").with(admin).with(csrf())
                        .param("codigo", "FRM-005").param("nome", "")
                        .param("caracteristicas", "aço cromado")
                        .param("estoqueAtual", "10").param("estoqueMinimo", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/produtocadastrar"))
                .andExpect(model().attributeHasFieldErrors("produtoDto", "nome"));

        assertThat(produtoRepository.existsByCodigo("FRM-005")).isFalse();
    }

    @Test
    @DisplayName("CT-08 — Código duplicado: 'Produto já cadastrado' e nada é gravado (RN-001)")
    void ct08_cadastrarComCodigoDuplicado() throws Exception {
        long antes = produtoRepository.count();

        mockMvc.perform(post("/produtocadastrar").with(admin).with(csrf())
                        .param("codigo", "FRM-001").param("nome", "Chave Phillips 4 polegadas")
                        .param("caracteristicas", "ponta cromada")
                        .param("estoqueAtual", "20").param("estoqueMinimo", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/produtocadastrar"))
                .andExpect(model().attribute("erroCodigoDuplicado", "Produto já cadastrado"));

        assertThat(produtoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("CT-09 — Estoque inicial negativo é rejeitado e nada é gravado (RN-002)")
    void ct09_cadastrarComEstoqueNegativo() throws Exception {
        mockMvc.perform(post("/produtocadastrar").with(admin).with(csrf())
                        .param("codigo", "FRM-006").param("nome", "Serrote 12 polegadas")
                        .param("caracteristicas", "lâmina de aço")
                        .param("estoqueAtual", "-5").param("estoqueMinimo", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/produtocadastrar"))
                .andExpect(model().attributeHasFieldErrors("produtoDto", "estoqueAtual"));

        assertThat(produtoRepository.existsByCodigo("FRM-006")).isFalse();
    }

    // ---------------------------------------------------------------- RF-004 Listar e pesquisar

    @Test
    @DisplayName("CT-10 — Listagem automática traz os produtos cadastrados")
    void ct10_listagemAutomatica() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/produtolista").with(admin))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/produtolista"))
                .andReturn();

        List<ProdutoRespostaDto> lista = listaDoModelo(resultado, "listaProdutos");
        assertThat(lista).extracting(ProdutoRespostaDto::codigo)
                .contains("FRM-001", "FRM-002", "FRM-003");
    }

    @Test
    @DisplayName("CT-11 — Busca por 'martelo' retorna só o FRM-001")
    void ct11_buscaComResultado() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/produtolista").param("termo", "martelo").with(admin))
                .andExpect(status().isOk())
                .andReturn();

        List<ProdutoRespostaDto> lista = listaDoModelo(resultado, "listaProdutos");
        assertThat(lista).extracting(ProdutoRespostaDto::codigo).containsExactly("FRM-001");
    }

    @Test
    @DisplayName("CT-12 — Busca sem correspondência mostra 'Nenhum produto encontrado.'")
    void ct12_buscaSemResultado() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/produtolista").param("termo", "parafusadeira").with(admin))
                .andExpect(status().isOk())
                .andReturn();

        List<ProdutoRespostaDto> lista = listaDoModelo(resultado, "listaProdutos");
        assertThat(lista).isEmpty();
        assertThat(resultado.getResponse().getContentAsString()).contains("Nenhum produto encontrado.");
    }

    @Test
    @DisplayName("Busca vazia (FA-01 do RF-004) exibe novamente todos os produtos")
    void buscaVaziaExibeTodos() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/produtolista").param("termo", "").with(admin))
                .andExpect(status().isOk())
                .andReturn();

        List<ProdutoRespostaDto> lista = listaDoModelo(resultado, "listaProdutos");
        assertThat(lista).hasSizeGreaterThanOrEqualTo(3);
    }

    // ---------------------------------------------------------------- RF-005 Editar

    @Test
    @DisplayName("CT-13 — Editar características atualiza o produto no banco")
    void ct13_editarProduto() throws Exception {
        mockMvc.perform(post("/produtoatualizar/{id}", martelo.getId()).with(admin).with(csrf())
                        .param("id", martelo.getId().toString())
                        .param("codigo", "FRM-001").param("nome", "Martelo Unha")
                        .param("caracteristicas", "cabo de fibra de vidro, aço forjado")
                        .param("estoqueAtual", "50").param("estoqueMinimo", "10"))
                .andExpect(redirectedUrl("/produtolista"))
                .andExpect(flash().attribute("mensagemAtualizacao", "Produto atualizado com sucesso!"));

        assertThat(recarregar(martelo).getCaracteristicas()).isEqualTo("cabo de fibra de vidro, aço forjado");
    }

    @Test
    @DisplayName("CT-14 — Editar com nome em branco é rejeitado e o produto não muda")
    void ct14_editarComNomeEmBranco() throws Exception {
        mockMvc.perform(post("/produtoatualizar/{id}", martelo.getId()).with(admin).with(csrf())
                        .param("id", martelo.getId().toString())
                        .param("codigo", "FRM-001").param("nome", "")
                        .param("caracteristicas", "Cabo de madeira")
                        .param("estoqueAtual", "50").param("estoqueMinimo", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/produtoatualizar"))
                .andExpect(model().attributeHasFieldErrors("produtoAtualizacao", "nome"));

        assertThat(recarregar(martelo).getNome()).isEqualTo("Martelo Unha");
    }

    // ---------------------------------------------------------------- RF-006 Excluir

    @Test
    @DisplayName("CT-15 — Excluir produto sem movimentações remove do banco")
    void ct15_excluirSemMovimentacao() throws Exception {
        ProdutoEntity trena = salvarProduto("FRM-004", "Trena 5m", "case plástico", 15, 10);

        mockMvc.perform(delete("/produtoexcluir/{id}", trena.getId()).with(admin).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(produtoRepository.findById(trena.getId())).isEmpty();
    }

    @Test
    @DisplayName("CT-16 — Excluir produto com movimentação é bloqueado (400) e o produto permanece (RN-005)")
    void ct16_excluirComMovimentacaoBloqueado() throws Exception {
        registrarMovimentacaoDireta(chave, TipoMovimentacaoEstoque.ENTRADA, 10, LocalDateTime.now());

        mockMvc.perform(delete("/produtoexcluir/{id}", chave.getId()).with(admin).with(csrf()))
                .andExpect(status().isBadRequest());

        assertThat(produtoRepository.findById(chave.getId())).isPresent();
    }

    // ---------------------------------------------------------------- RF-007 Movimentar

    private void movimentar(String tipo, ProdutoEntity produto, int quantidade,
                            org.springframework.test.web.servlet.ResultMatcher... esperado) throws Exception {
        var requisicao = post("/movimentacaocadastrar").with(usuarioComum).with(csrf())
                .param("produtoId", produto.getId().toString())
                .param("quantidade", String.valueOf(quantidade));
        if (tipo != null) {
            requisicao.param("tipo", tipo);
        }
        var acao = mockMvc.perform(requisicao);
        for (var matcher : esperado) {
            acao.andExpect(matcher);
        }
    }

    @Test
    @DisplayName("CT-17 — Entrada soma ao estoque (80 + 10 = 90) e grava histórico com o usuário")
    void ct17_entrada() throws Exception {
        movimentar("ENTRADA", chave, 10,
                redirectedUrl("/movimentacoes"),
                flash().attribute("mensagemSucesso", "Movimentação de entrada registrada com sucesso!"));

        assertThat(recarregar(chave).getEstoqueAtual()).isEqualTo(90);
        List<MovimentacaoEstoqueEntity> historico = movimentacaoRepository.findAllByOrderByDataMovimentacaoDesc();
        assertThat(historico).hasSize(1);
        assertThat(historico.get(0).getUsuario().getLogin()).isEqualTo("maria");
        assertThat(historico.get(0).getTipoMovimentacao()).isEqualTo(TipoMovimentacaoEstoque.ENTRADA);
    }

    @Test
    @DisplayName("CT-18 — Saída com estoque suficiente (50 - 5 = 45), sem alerta")
    void ct18_saidaSuficiente() throws Exception {
        movimentar("SAIDA", martelo, 5,
                redirectedUrl("/movimentacoes"),
                flash().attribute("mensagemSucesso", "Movimentação de saída registrada com sucesso!"));

        assertThat(recarregar(martelo).getEstoqueAtual()).isEqualTo(45);
    }

    @Test
    @DisplayName("CT-19 — Saída maior que o estoque: erro e saldo inalterado")
    void ct19_saidaInsuficiente() throws Exception {
        martelo.setEstoqueAtual(45);
        produtoRepository.save(martelo);

        movimentar("SAIDA", martelo, 60,
                status().isOk(),
                view().name("movimentacoes/listarmovimentacoes"),
                model().attribute("erroQuantidade", "Quantidade insuficiente em estoque"));

        assertThat(recarregar(martelo).getEstoqueAtual()).isEqualTo(45);
        assertThat(movimentacaoRepository.count()).isZero();
    }

    @Test
    @DisplayName("CT-20 — Saída que deixa exatamente no mínimo (30 - 25 = 5): sem alerta")
    void ct20_limiteIgualAoMinimo() throws Exception {
        movimentar("SAIDA", alicate, 25,
                redirectedUrl("/movimentacoes"),
                flash().attribute("mensagemSucesso", "Movimentação de saída registrada com sucesso!"));

        assertThat(recarregar(alicate).getEstoqueAtual()).isEqualTo(5);
    }

    @Test
    @DisplayName("CT-21 — Saída que deixa uma unidade abaixo do mínimo (30 - 26 = 4): alerta (RN-004)")
    void ct21_limiteAbaixoDoMinimo() throws Exception {
        movimentar("SAIDA", alicate, 26,
                redirectedUrl("/movimentacoes"),
                flash().attribute("alertaMinimo",
                        "Movimentação registrada com sucesso! ATENÇÃO: O produto 'Alicate Universal' "
                                + "ficou abaixo do estoque mínimo (5 unidades)."));

        assertThat(recarregar(alicate).getEstoqueAtual()).isEqualTo(4);
    }

    @Test
    @DisplayName("CT-22 — Saída que deixa uma unidade acima do mínimo (30 - 24 = 6): sem alerta")
    void ct22_limiteAcimaDoMinimo() throws Exception {
        movimentar("SAIDA", alicate, 24,
                redirectedUrl("/movimentacoes"),
                flash().attribute("mensagemSucesso", "Movimentação de saída registrada com sucesso!"));

        assertThat(recarregar(alicate).getEstoqueAtual()).isEqualTo(6);
    }

    @Test
    @DisplayName("CT-25 — Movimentação sem tipo é recusada pelo servidor e nada é gravado")
    void ct25_movimentacaoSemTipo() throws Exception {
        movimentar(null, martelo, 5,
                status().isOk(),
                view().name("movimentacoes/listarmovimentacoes"),
                model().attribute("mensagemErro", "O tipo de movimentação é obrigatório."));

        assertThat(recarregar(martelo).getEstoqueAtual()).isEqualTo(50);
        assertThat(movimentacaoRepository.count()).isZero();
    }

    @Test
    @DisplayName("CT-25 (variação) — Movimentação sem quantidade é recusada pelo servidor")
    void ct25_movimentacaoSemQuantidade() throws Exception {
        mockMvc.perform(post("/movimentacaocadastrar").with(usuarioComum).with(csrf())
                        .param("produtoId", martelo.getId().toString())
                        .param("tipo", "ENTRADA"))
                .andExpect(status().isOk())
                .andExpect(view().name("movimentacoes/listarmovimentacoes"))
                .andExpect(model().attribute("mensagemErro", "A quantidade é obrigatória"));

        assertThat(movimentacaoRepository.count()).isZero();
    }

    @Test
    @DisplayName("CT-27 — Saída que zera o estoque (30 - 30 = 0): aceita, com alerta de mínimo")
    void ct27_saidaQueZeraOEstoque() throws Exception {
        movimentar("SAIDA", alicate, 30,
                redirectedUrl("/movimentacoes"),
                flash().attributeExists("alertaMinimo"));

        assertThat(recarregar(alicate).getEstoqueAtual()).isZero();
    }

    // ---------------------------------------------------------------- RF-008 Histórico

    private void registrarMovimentacaoDireta(ProdutoEntity produto, TipoMovimentacaoEstoque tipo,
                                             int quantidade, LocalDateTime data) {
        MovimentacaoEstoqueEntity m = new MovimentacaoEstoqueEntity();
        m.setProduto(produto);
        m.setUsuario(usuarioRepository.findByLogin("maria").orElseThrow());
        m.setTipoMovimentacao(tipo);
        m.setQuantidade(quantidade);
        m.setDataMovimentacao(data);
        movimentacaoRepository.save(m);
    }

    @Test
    @DisplayName("CT-23 — Histórico lista as movimentações da mais recente para a mais antiga")
    void ct23_historicoOrdenado() throws Exception {
        registrarMovimentacaoDireta(chave, TipoMovimentacaoEstoque.ENTRADA, 10, LocalDateTime.now().minusDays(2));
        registrarMovimentacaoDireta(martelo, TipoMovimentacaoEstoque.SAIDA, 5, LocalDateTime.now());

        MvcResult resultado = mockMvc.perform(get("/movimentacoes").with(usuarioComum))
                .andExpect(status().isOk())
                .andExpect(view().name("movimentacoes/listarmovimentacoes"))
                .andReturn();

        List<MovimentacaoEstoqueEntity> historico = listaDoModelo(resultado, "listaMovimentacoes");
        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).getDataMovimentacao()).isAfter(historico.get(1).getDataMovimentacao());
        assertThat(historico.get(0).getProduto().getCodigo()).isEqualTo("FRM-001");
    }

    @Test
    @DisplayName("CT-24 — Histórico vazio mostra 'Ainda não há movimentações registradas'")
    void ct24_historicoVazio() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/movimentacoes").with(usuarioComum))
                .andExpect(status().isOk())
                .andReturn();

        List<MovimentacaoEstoqueEntity> historico = listaDoModelo(resultado, "listaMovimentacoes");
        assertThat(historico).isEmpty();
        assertThat(resultado.getResponse().getContentAsString())
                .contains("Ainda não há movimentações registradas");
    }
}
