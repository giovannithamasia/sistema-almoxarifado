package com.senai.sistema_almoxarifado.repository;

import com.senai.sistema_almoxarifado.entity.MovimentacaoEstoqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoqueEntity,Long> {

    List<MovimentacaoEstoqueEntity> findAllByOrderByDataMovimentacaoDesc();
}
