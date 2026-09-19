package com.senai.sistema_almoxarifado.dto.MovimentacaoDto;

import java.time.LocalDateTime;

public record MovimentacaoRequisicaoDto(
        Long idProduto,
        Integer quantidade,
        LocalDateTime dataMovimentacao){
}