package com.senai.sistema_almoxarifado.service;

import com.senai.sistema_almoxarifado.dto.movimentacao.MovimentacaoEstoqueDto;
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
    public void registrarEntrada(MovimentacaoEstoqueDto dto, UsuarioEntity usuarioLogado) {
        ProdutoEntity produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado no sistema."));

        produto.setEstoqueAtual(produto.getEstoqueAtual() + dto.quantidade());
        produtoRepository.save(produto);

        movimentacaoEstoqueRepository.save(dto.toMovimentacao(produto,usuarioLogado,
                TipoMovimentacaoEstoque.ENTRADA));
    }

    @Transactional
    public void registrarSaida(MovimentacaoEstoqueDto dto, UsuarioEntity usuarioLogado) {
        ProdutoEntity produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado no sistema"));

        if (dto.quantidade() > produto.getEstoqueAtual()) {
            throw new IllegalArgumentException("Quantidade insuficiente em estoque");
        }

        produto.setEstoqueAtual(produto.getEstoqueAtual() - dto.quantidade());
        produtoRepository.save(produto);

        movimentacaoEstoqueRepository.save(dto.toMovimentacao(produto,usuarioLogado,
                TipoMovimentacaoEstoque.SAIDA));
    }

    public boolean verificarAlertaEstoqueMinimo(Long produtoId) {
        ProdutoEntity produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        return produto.getEstoqueAtual() < produto.getEstoqueMinimo();
    }
}

