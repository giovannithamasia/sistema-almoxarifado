package com.senai.sistema_almoxarifado.dto.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoAtualizarDto(
        Long id,
        String codigo,

        @Size(min = 2, max = 45)
        @NotBlank(message = "Campo obrigatório")
        String nome,

        @Size(max = 45)
        @NotBlank(message = "Campo obrigatório")
        String caracteristicas,

        Integer estoqueAtual,

        @NotNull(message = "Campo obrigatório")
        @PositiveOrZero(message = "Valor inválido")
        Integer estoqueMinimo
) {
}
