package com.senai.sistema_almoxarifado.repository;

import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity,Long> {

    List<ProdutoEntity> findByCodigoContainingIgnoreCaseOrNomeContainingIgnoreCase(String codigo, String nome);
}
