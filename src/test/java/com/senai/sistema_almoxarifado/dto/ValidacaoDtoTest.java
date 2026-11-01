package com.senai.sistema_almoxarifado.dto;

import com.senai.sistema_almoxarifado.dto.movimentacao.MovimentacaoEstoqueDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoAtualizarDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ValidacaoDtoTest {

    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static Set<String> camposInvalidos(Object dto) {
        return validator.validate(dto).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static String mensagemDoCampo(Object dto, String campo) {
        return validator.validate(dto).stream()
                .filter(v -> v.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse(null);
    }

    @Test
    @DisplayName("CT-06 — Dados válidos de cadastro não geram violações")
    void ct06_cadastroValidoSemViolacoes() {
        ProdutoDto dto = new ProdutoDto("FRM-004", "Trena 5m", "case plástico, fita de aço", 15, 10);

        assertThat(camposInvalidos(dto)).isEmpty();
    }

    @Test
    @DisplayName("CT-07 — Nome em branco no cadastro é rejeitado (FE-01 do RF-003)")
    void ct07_nomeEmBrancoNoCadastro() {
        ProdutoDto dto = new ProdutoDto("FRM-005", "", "aço cromado", 10, 5);

        assertThat(camposInvalidos(dto)).contains("nome");
    }

    @Test
    @DisplayName("CT-07 (variação) — Código, características e estoques ausentes são rejeitados")
    void ct07_outrosCamposObrigatorios() {
        ProdutoDto dto = new ProdutoDto("", "Serrote", "", null, null);

        assertThat(camposInvalidos(dto)).contains("codigo", "caracteristicas", "estoqueAtual", "estoqueMinimo");
    }

    @Test
    @DisplayName("CT-09 — Estoque inicial negativo exibe 'Valor inválido' (FE-02 do RF-003, RN-002)")
    void ct09_estoqueNegativoNoCadastro() {
        ProdutoDto dto = new ProdutoDto("FRM-006", "Serrote 12 polegadas", "lâmina de aço", -5, 10);

        assertThat(camposInvalidos(dto)).contains("estoqueAtual");
        assertThat(mensagemDoCampo(dto, "estoqueAtual")).isEqualTo("Valor inválido");
    }

    @Test
    @DisplayName("CT-09 (variação) — Estoque mínimo negativo também é rejeitado")
    void ct09_estoqueMinimoNegativoNoCadastro() {
        ProdutoDto dto = new ProdutoDto("FRM-006", "Serrote 12 polegadas", "lâmina de aço", 10, -1);

        assertThat(camposInvalidos(dto)).contains("estoqueMinimo");
    }

    @Test
    @DisplayName("CT-14 — Nome em branco na edição é rejeitado (FE-01 do RF-005)")
    void ct14_nomeEmBrancoNaEdicao() {
        ProdutoAtualizarDto dto = new ProdutoAtualizarDto(1L, "FRM-001", "  ", "Cabo de madeira", 50, 10);

        assertThat(camposInvalidos(dto)).contains("nome");
    }

    @Test
    @DisplayName("CT-25 — Movimentação sem quantidade é rejeitada (FE-03 do RF-007)")
    void ct25_movimentacaoSemQuantidade() {
        MovimentacaoEstoqueDto dto = new MovimentacaoEstoqueDto(1L, null);

        assertThat(camposInvalidos(dto)).contains("quantidade");
    }

    @Test
    @DisplayName("CT-25 (variação) — Movimentação sem produto é rejeitada")
    void ct25_movimentacaoSemProduto() {
        MovimentacaoEstoqueDto dto = new MovimentacaoEstoqueDto(null, 5);

        assertThat(camposInvalidos(dto)).contains("produtoId");
    }

    @Test
    @DisplayName("CT-25 (variação) — Quantidade zero ou negativa na movimentação é rejeitada")
    void ct25_movimentacaoComQuantidadeZero() {
        assertThat(camposInvalidos(new MovimentacaoEstoqueDto(1L, 0))).contains("quantidade");
        assertThat(camposInvalidos(new MovimentacaoEstoqueDto(1L, -3))).contains("quantidade");
    }
}
