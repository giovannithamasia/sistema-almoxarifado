package com.senai.sistema_almoxarifado.service;

import com.senai.sistema_almoxarifado.dto.MovimentacaoDto.MovimentacaoRequisicaoDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoRespostaDto;
import com.senai.sistema_almoxarifado.entity.MovimentacaoEstoqueEntity;
import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import com.senai.sistema_almoxarifado.entity.TipoMovimentacaoEstoque;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.repository.MovimentacaoEstoqueRepository;
import com.senai.sistema_almoxarifado.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ProdutoRepository produtoRepository;

    public List<ProdutoRespostaDto> listarProdutosCadastrados() {
        return produtoRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(ProdutoRespostaDto::toProdutoRespostaDto)
                .toList();
    }

    public List<MovimentacaoEstoqueEntity> listarHistoricoMovimentacoes() {
        return movimentacaoEstoqueRepository.findAllByOrderByDataMovimentacaoDesc();
    }

    @Transactional
    public String registrarEntrada(MovimentacaoRequisicaoDto dto, UsuarioEntity usuarioLogado) {
        if (dto.idProduto() == null || dto.quantidade() == null || dto.dataMovimentacao() == null) {
            throw new IllegalArgumentException("Campos obrigatórios (Produto, Quantidade e Data) não preenchidos.");
        }
        if (dto.quantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade de entrada deve ser maior que zero.");
        }

        ProdutoEntity produto = produtoRepository.findById(dto.idProduto())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado no sistema."));

        produto.setEstoqueAtual(produto.getEstoqueAtual() + dto.quantidade());
        produtoRepository.save(produto);

        MovimentacaoEstoqueEntity movimentacao = new MovimentacaoEstoqueEntity();
        movimentacao.setProduto(produto);
        movimentacao.setUsuario(usuarioLogado);
        movimentacao.setTipoMovimentacao(TipoMovimentacaoEstoque.ENTRADA);
        movimentacao.setQuantidade(dto.quantidade());
        movimentacao.setDataMovimentacao(dto.dataMovimentacao());
        movimentacaoEstoqueRepository.save(movimentacao);

        return "Movimentação de entrada registrada com sucesso!";
    }

    @Transactional
    public String registrarSaida(MovimentacaoRequisicaoDto dto, UsuarioEntity usuarioLogado) {
        if (dto.idProduto() == null || dto.quantidade() == null || dto.dataMovimentacao() == null) {
            throw new IllegalArgumentException("Campos obrigatórios (Produto, Quantidade e Data) não preenchidos.");
        }
        if (dto.quantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade de saída deve ser maior que zero.");
        }

        ProdutoEntity produto = produtoRepository.findById(dto.idProduto())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado no sistema."));

        if (dto.quantidade() > produto.getEstoqueAtual()) {
            throw new IllegalArgumentException("Quantidade insuficiente em estoque.");
        }

        produto.setEstoqueAtual(produto.getEstoqueAtual() - dto.quantidade());
        produtoRepository.save(produto);

        MovimentacaoEstoqueEntity movimentacao = new MovimentacaoEstoqueEntity();
        movimentacao.setProduto(produto);
        movimentacao.setUsuario(usuarioLogado);
        movimentacao.setTipoMovimentacao(TipoMovimentacaoEstoque.SAIDA);
        movimentacao.setQuantidade(dto.quantidade());
        movimentacao.setDataMovimentacao(dto.dataMovimentacao());
        movimentacaoEstoqueRepository.save(movimentacao);

        int estoqueMin = produto.getEstoqueMinimo() != null ? produto.getEstoqueMinimo() : 0;
        if (produto.getEstoqueAtual() < estoqueMin) {
            return "Movimentação registrada com sucesso! ATENÇÃO: O produto " + produto.getNome() + " ficou abaixo do estoque mínimo.";
        }

        return "Movimentação de saída registrada com sucesso!";
    }
}

