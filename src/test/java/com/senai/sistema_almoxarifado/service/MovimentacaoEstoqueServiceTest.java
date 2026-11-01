package com.senai.sistema_almoxarifado.service;

import com.senai.sistema_almoxarifado.dto.movimentacao.MovimentacaoEstoqueDto;
import com.senai.sistema_almoxarifado.entity.MovimentacaoEstoqueEntity;
import com.senai.sistema_almoxarifado.entity.Papel;
import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import com.senai.sistema_almoxarifado.entity.TipoMovimentacaoEstoque;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private MovimentacaoEstoqueService service;

    private final UsuarioEntity maria =
            new UsuarioEntity(2L, "Maria", "maria", "senha-criptografada", Papel.ROLE_USER);

    private ProdutoEntity produto(Long id, String codigo, String nome, int estoqueAtual, int estoqueMinimo) {
        ProdutoEntity p = new ProdutoEntity();
        p.setId(id);
        p.setCodigo(codigo);
        p.setNome(nome);
        p.setCaracteristicas("teste");
        p.setEstoqueAtual(estoqueAtual);
        p.setEstoqueMinimo(estoqueMinimo);
        return p;
    }

    /** Executa uma saída e devolve o alerta de estoque mínimo (null = sem alerta). */
    private String saidaEAlerta(ProdutoEntity produto, int quantidade) {
        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        service.registrarSaida(new MovimentacaoEstoqueDto(produto.getId(), quantidade), maria);
        return service.verificarAlertaEstoqueMinimo(produto.getId());
    }

    @Test
    @DisplayName("CT-17 — Registrar entrada de estoque (80 + 10 = 90)")
    void ct17_registrarEntrada() {
        ProdutoEntity chave = produto(2L, "FRM-002", "Chave de Fenda", 80, 15);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(chave));

        service.registrarEntrada(new MovimentacaoEstoqueDto(2L, 10), maria);

        assertThat(chave.getEstoqueAtual()).isEqualTo(90);
        ArgumentCaptor<MovimentacaoEstoqueEntity> captor = ArgumentCaptor.forClass(MovimentacaoEstoqueEntity.class);
        verify(movimentacaoRepository).save(captor.capture());
        MovimentacaoEstoqueEntity mov = captor.getValue();
        assertThat(mov.getTipoMovimentacao()).isEqualTo(TipoMovimentacaoEstoque.ENTRADA);
        assertThat(mov.getQuantidade()).isEqualTo(10);
        assertThat(mov.getUsuario().getLogin()).isEqualTo("maria");   // RN-003
        assertThat(mov.getDataMovimentacao()).isNotNull();            // RN-003
    }

    @Test
    @DisplayName("CT-18 — Registrar saída com estoque suficiente (50 - 5 = 45, sem alerta)")
    void ct18_registrarSaidaSuficiente() {
        ProdutoEntity martelo = produto(1L, "FRM-001", "Martelo Unha", 50, 10);

        String alerta = saidaEAlerta(martelo, 5);

        assertThat(martelo.getEstoqueAtual()).isEqualTo(45);
        assertThat(alerta).isNull();
        verify(movimentacaoRepository).save(any(MovimentacaoEstoqueEntity.class));
    }

    @Test
    @DisplayName("CT-19 — Saída maior que o estoque é bloqueada (RN-002)")
    void ct19_saidaMaiorQueEstoque() {
        ProdutoEntity martelo = produto(1L, "FRM-001", "Martelo Unha", 45, 10);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(martelo));

        assertThatThrownBy(() -> service.registrarSaida(new MovimentacaoEstoqueDto(1L, 60), maria))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade insuficiente em estoque");

        assertThat(martelo.getEstoqueAtual()).isEqualTo(45);
        verify(movimentacaoRepository, never()).save(any(MovimentacaoEstoqueEntity.class));
    }

    @Test
    @DisplayName("CT-20 — Saída que deixa o estoque IGUAL ao mínimo (30 - 25 = 5): sem alerta")
    void ct20_limiteIgualAoMinimo() {
        ProdutoEntity alicate = produto(3L, "FRM-003", "Alicate Universal", 30, 5);

        String alerta = saidaEAlerta(alicate, 25);

        assertThat(alicate.getEstoqueAtual()).isEqualTo(5);
        assertThat(alerta).isNull();
    }

    @Test
    @DisplayName("CT-21 — Saída que deixa o estoque ABAIXO do mínimo (30 - 26 = 4): com alerta (RN-004)")
    void ct21_limiteAbaixoDoMinimo() {
        ProdutoEntity alicate = produto(3L, "FRM-003", "Alicate Universal", 30, 5);

        String alerta = saidaEAlerta(alicate, 26);

        assertThat(alicate.getEstoqueAtual()).isEqualTo(4);
        assertThat(alerta).isEqualTo("O produto 'Alicate Universal' ficou abaixo do estoque mínimo (5 unidades).");
    }

    @Test
    @DisplayName("CT-22 — Saída que deixa o estoque ACIMA do mínimo (30 - 24 = 6): sem alerta")
    void ct22_limiteAcimaDoMinimo() {
        ProdutoEntity alicate = produto(3L, "FRM-003", "Alicate Universal", 30, 5);

        String alerta = saidaEAlerta(alicate, 24);

        assertThat(alicate.getEstoqueAtual()).isEqualTo(6);
        assertThat(alerta).isNull();
    }

    @Test
    @DisplayName("CT-27 — Saída que zera o estoque (30 - 30 = 0): aceita, nunca negativo, com alerta")
    void ct27_saidaQueZeraOEstoque() {
        ProdutoEntity alicate = produto(3L, "FRM-003", "Alicate Universal", 30, 5);

        String alerta = saidaEAlerta(alicate, 30);

        assertThat(alicate.getEstoqueAtual()).isZero();
        assertThat(alerta).isNotNull();
    }

    @Test
    @DisplayName("CT-23 — Histórico com registros vem do repositório ordenado do mais recente ao mais antigo")
    void ct23_historicoComRegistros() {
        MovimentacaoEstoqueEntity mov = new MovimentacaoEstoqueEntity();
        when(movimentacaoRepository.findAllByOrderByDataMovimentacaoDesc()).thenReturn(List.of(mov));

        assertThat(service.listarHistoricoMovimentacoes()).containsExactly(mov);
        verify(movimentacaoRepository).findAllByOrderByDataMovimentacaoDesc();
    }

    @Test
    @DisplayName("CT-24 — Histórico sem registros devolve lista vazia")
    void ct24_historicoVazio() {
        when(movimentacaoRepository.findAllByOrderByDataMovimentacaoDesc()).thenReturn(List.of());

        assertThat(service.listarHistoricoMovimentacoes()).isEmpty();
    }
}
