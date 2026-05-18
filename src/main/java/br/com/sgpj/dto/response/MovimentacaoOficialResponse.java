package br.com.sgpj.dto.response;

import java.time.LocalDateTime;

public record MovimentacaoOficialResponse(
        Long id,
        String codigo,
        String descricao,
        LocalDateTime dataHoraMovimentacao
) {
}
