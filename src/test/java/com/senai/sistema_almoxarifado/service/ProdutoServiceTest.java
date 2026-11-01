package com.senai.sistema_almoxarifado.service;

import com.senai.sistema_almoxarifado.dto.produto.ProdutoAtualizarDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoRespostaDto;
import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import com.senai.sistema_almoxarifado.exceptions.ProdutoCadastradoException;
import com.senai.sistema_almoxarifado.exceptions.ProdutoComMovimentacaoException;
import com.senai.sistema_almoxarifado.exceptions.ProdutoNaoEncontradoException;
import com.senai.sistema_almoxarifado.repository.MovimentacaoEstoqueRepository;
import com.senai.sistema_almoxarifado.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @InjectMocks
    private ProdutoService service;

    private ProdutoEntity produto(Long id, String codigo, String nome, String caracteristicas,
                                  int estoqueAtual, int estoqueMinimo) {
        ProdutoEntity p = new ProdutoEntity();
        p.setId(id);
        p.setCodigo(codigo);
        p.setNome(nome);
        p.setCaracteristicas(caracteristicas);
        p.setEstoqueAtual(estoqueAtual);
        p.setEstoqueMinimo(estoqueMinimo);
        return p;
    }

    @Test
    @DisplayName("CT-06 — Cadastrar produto com dados válidos")
    void ct06_cadastrarProdutoValido() {
        ProdutoDto dto = new ProdutoDto("FRM-004", "Trena 5m", "case plástico, fita de aço", 15, 10);
        when(repository.existsByCodigo("FRM-004")).thenReturn(false);

        service.cadastrarProduto(dto);

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repository).save(captor.capture());
        ProdutoEntity salvo = captor.getValue();
        assertThat(salvo.getCodigo()).isEqualTo("FRM-004");
        assertThat(salvo.getNome()).isEqualTo("Trena 5m");
        assertThat(salvo.getEstoqueAtual()).isEqualTo(15);
        assertThat(salvo.getEstoqueMinimo()).isEqualTo(10);
    }

    @Test
    @DisplayName("CT-08 — Cadastrar produto com código já cadastrado (RN-001)")
    void ct08_cadastrarProdutoComCodigoDuplicado() {
        ProdutoDto dto = new ProdutoDto("FRM-001", "Chave Phillips 4 polegadas", "ponta cromada", 20, 10);
        when(repository.existsByCodigo("FRM-001")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarProduto(dto))
                .isInstanceOf(ProdutoCadastradoException.class)
                .hasMessage("Produto já cadastrado");

        verify(repository, never()).save(any(ProdutoEntity.class));
    }

    @Test
    @DisplayName("CT-11 — Pesquisar produto com termo existente")
    void ct11_buscarComResultado() {
        ProdutoEntity martelo = produto(1L, "FRM-001", "Martelo Unha", "Cabo de madeira", 50, 10);
        when(repository.findByCodigoContainingIgnoreCaseOrNomeContainingIgnoreCase("martelo", "martelo"))
                .thenReturn(List.of(martelo));

        List<ProdutoRespostaDto> resultado = service.buscarPersonalizada("martelo");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).codigo()).isEqualTo("FRM-001");
    }

    @Test
    @DisplayName("CT-12 — Pesquisar termo sem correspondência")
    void ct12_buscarSemResultado() {
        when(repository.findByCodigoContainingIgnoreCaseOrNomeContainingIgnoreCase("parafusadeira", "parafusadeira"))
                .thenReturn(List.of());

        assertThat(service.buscarPersonalizada("parafusadeira")).isEmpty();
    }

    @Test
    @DisplayName("CT-13 — Editar dados de um produto")
    void ct13_atualizarProduto() {
        ProdutoEntity martelo = produto(1L, "FRM-001", "Martelo Unha", "Cabo de madeira", 50, 10);
        when(repository.findById(1L)).thenReturn(Optional.of(martelo));
        ProdutoAtualizarDto dto = new ProdutoAtualizarDto(1L, "FRM-001", "Martelo Unha",
                "cabo de fibra de vidro, aço forjado", 50, 10);

        service.atualizarProduto(1L, dto);

        verify(repository).save(martelo);
        assertThat(martelo.getCaracteristicas()).isEqualTo("cabo de fibra de vidro, aço forjado");
    }

    @Test
    @DisplayName("Editar produto inexistente lança ProdutoNaoEncontradoException")
    void atualizarProdutoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        ProdutoAtualizarDto dto = new ProdutoAtualizarDto(99L, "X", "Nome", "Carac", 1, 1);

        assertThatThrownBy(() -> service.atualizarProduto(99L, dto))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("CT-15 — Excluir produto sem movimentações vinculadas")
    void ct15_excluirProdutoSemMovimentacao() {
        ProdutoEntity trena = produto(4L, "FRM-004", "Trena 5m", "case plástico", 15, 10);
        when(repository.findById(4L)).thenReturn(Optional.of(trena));
        when(movimentacaoRepository.existsByProdutoId(4L)).thenReturn(false);

        service.excluirProduto(4L);

        verify(repository).deleteById(4L);
    }

    @Test
    @DisplayName("CT-16 — Excluir produto com movimentações vinculadas (RN-005)")
    void ct16_excluirProdutoComMovimentacaoEhBloqueado() {
        ProdutoEntity chave = produto(2L, "FRM-002", "Chave de Fenda", "Ponta chata", 80, 15);
        when(repository.findById(2L)).thenReturn(Optional.of(chave));
        when(movimentacaoRepository.existsByProdutoId(2L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluirProduto(2L))
                .isInstanceOf(ProdutoComMovimentacaoException.class)
                .hasMessage("Não é possível excluir o produto pois ele possui movimentações registradas");

        verify(repository, never()).deleteById(anyLong());
    }
}
