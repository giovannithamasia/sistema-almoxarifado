package com.senai.sistema_almoxarifado.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoqueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_produto",nullable = false)
    private ProdutoEntity produto;

    @ManyToOne
    @JoinColumn(name = "id_usuario",nullable = false)
    private UsuarioEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao",length = 10,nullable = false)
    private TipoMovimentacaoEstoque tipoMovimentacao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "data_movimentacao",nullable = false)
    private LocalDateTime dataMovimentacao;
}
