package com.senai.sistema_almoxarifado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoAtualizarDto(

        @Size(min = 2, max = 45)
        @NotBlank
        String nome,

        @Size(max = 45)
        @NotBlank
        String caracteristicas,

        @NotNull
        @PositiveOrZero
        Integer estoqueAtual,

        @NotNull
        @PositiveOrZero
        Integer estoqueMinimo
) {
}
