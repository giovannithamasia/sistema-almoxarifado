package com.senai.sistema_almoxarifado.repository;

import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity,Long> {
}
