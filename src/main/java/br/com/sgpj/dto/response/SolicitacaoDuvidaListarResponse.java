package br.com.sgpj.dto.response;

import br.com.sgpj.entity.SolicitacaoStatus;

import java.time.LocalDateTime;

public record SolicitacaoDuvidaListarResponse(
        Long id,
        Long clienteId,
        String nomeCompleto,
        String email,
        String telefone,
        String estado,
        String numeroProcesso,
        String descricao,
        LocalDateTime dataSolicitacao,
        SolicitacaoStatus status
) {
}
