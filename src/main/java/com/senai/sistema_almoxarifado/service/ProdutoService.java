package com.senai.sistema_almoxarifado.service;

import com.senai.sistema_almoxarifado.dto.produto.ProdutoAtualizarDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoRespostaDto;
import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import com.senai.sistema_almoxarifado.exceptions.ProdutoCadastradoException;
import com.senai.sistema_almoxarifado.exceptions.ProdutoNaoEncontradoException;
import com.senai.sistema_almoxarifado.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;

    public List<ProdutoRespostaDto> listarProdutos(){
        return repository.findAll()
                .stream()
                .map(ProdutoRespostaDto::toProdutoRespostaDto)
                .toList();
    }

    public List<ProdutoRespostaDto> buscarPersonalizada(String termo){
        List<ProdutoEntity> produtos = repository.
                findByCodigoContainingIgnoreCaseOrNomeContainingIgnoreCase(termo, termo);

        return produtos.stream()
                .map(ProdutoRespostaDto::toProdutoRespostaDto)
                .toList();
    }

    public void cadastrarProduto(ProdutoDto produtoDto){
        if (repository.existsByCodigo(produtoDto.codigo())){
            throw new ProdutoCadastradoException("Produto já cadastrado");
        }

        repository.save(produtoDto.toProduto());
    }

    public ProdutoEntity buscarProdutoPorId(Long id){
        return repository.findById(id)
                .orElseThrow(() ->
                        new ProdutoNaoEncontradoException("Produto não encontrado"));
    }

    public void atualizarProduto(Long id, ProdutoAtualizarDto produtoAtualizarDto){
        ProdutoEntity produto = buscarProdutoPorId(id);

        produto.setNome(produtoAtualizarDto.nome());
        produto.setCaracteristicas(produtoAtualizarDto.caracteristicas());
        produto.setEstoqueAtual(produtoAtualizarDto.estoqueAtual());
        produto.setEstoqueMinimo(produtoAtualizarDto.estoqueMinimo());

        repository.save(produto);
    }

    public void excluirProduto(Long id){
        buscarProdutoPorId(id);

        repository.deleteById(id);
    }
}
