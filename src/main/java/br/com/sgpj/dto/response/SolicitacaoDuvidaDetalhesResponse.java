package br.com.sgpj.dto.response;

import br.com.sgpj.dto.response.Processos.ProcessoApiCreateResponse;
import br.com.sgpj.entity.SolicitacaoStatus;

import java.time.LocalDateTime;

public record SolicitacaoDuvidaDetalhesResponse(
        Long id,
        Long clienteId,
        String nomeCompleto,
        String email,
        String telefone,
        String estado,
        String numeroProcesso,
        String descricao,
        Boolean aceitouTermos,
        LocalDateTime dataSolicitacao,
        SolicitacaoStatus status,
        LocalDateTime dataResposta,
        ProcessoApiCreateResponse processo
) {
}
