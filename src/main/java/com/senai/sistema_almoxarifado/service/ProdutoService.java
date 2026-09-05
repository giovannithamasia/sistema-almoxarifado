package com.senai.sistema_almoxarifado.service;

import com.senai.sistema_almoxarifado.dto.ProdutoRespostaDto;
import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
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
}
