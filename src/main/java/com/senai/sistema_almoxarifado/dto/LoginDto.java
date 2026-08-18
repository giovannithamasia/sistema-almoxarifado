package com.senai.sistema_almoxarifado.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(

        @NotBlank(message = "O login não pode ser vazio nem nulo")
        String login,

        @NotBlank(message = "A senha não pode ser vazia nem nula")
        String senha

) {
}
