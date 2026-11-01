package com.senai.sistema_almoxarifado.dto.produto;

import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoDto(

      @Size(max = 45)
      @NotBlank(message = "Campo obrigatório")
      String codigo,

      @Size(min = 2, max = 45)
      @NotBlank(message = "Campo obrigatório")
      String nome,

      @Size(max = 45)
      @NotBlank(message = "Campo obrigatório")
      String caracteristicas,

      @NotNull(message = "Campo obrigatório")
      @PositiveOrZero(message = "Valor inválido")
      Integer estoqueAtual,

      @NotNull(message = "Campo obrigatório")
      @PositiveOrZero(message = "Valor inválido")
      Integer estoqueMinimo
) {

      public ProdutoEntity toProduto(){
            ProdutoEntity produto = new ProdutoEntity();

            produto.setNome(this.nome);
            produto.setCodigo(this.codigo);
            produto.setCaracteristicas(this.caracteristicas);
            produto.setEstoqueAtual(this.estoqueAtual);
            produto.setEstoqueMinimo(this.estoqueMinimo);

            return produto;
      }
}
