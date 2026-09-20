package com.senai.sistema_almoxarifado.dto.movimentacao;

import com.senai.sistema_almoxarifado.entity.MovimentacaoEstoqueEntity;
import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import com.senai.sistema_almoxarifado.entity.TipoMovimentacaoEstoque;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record MovimentacaoEstoqueDto(

        @NotNull(message = "O produto é obrigatório")
        Long produtoId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @NotNull(message = "A data da movimentação é obrigatória")
        LocalDateTime dataMovimentacao
) {

        public MovimentacaoEstoqueEntity toMovimentacao(ProdutoEntity produto, UsuarioEntity usuario, TipoMovimentacaoEstoque tipo) {
                MovimentacaoEstoqueEntity movimentacao = new MovimentacaoEstoqueEntity();
                movimentacao.setProduto(produto);
                movimentacao.setUsuario(usuario);
                movimentacao.setTipoMovimentacao(tipo);
                movimentacao.setQuantidade(this.quantidade());
                movimentacao.setDataMovimentacao(this.dataMovimentacao());
                return movimentacao;
        }

}