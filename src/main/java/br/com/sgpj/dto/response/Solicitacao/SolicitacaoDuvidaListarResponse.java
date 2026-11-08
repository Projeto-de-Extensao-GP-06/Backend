package br.com.sgpj.dto.response.Solicitacao;

import br.com.sgpj.entity.ComoConheceuEnum;
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
        ComoConheceuEnum comoConheceu,
        LocalDateTime dataSolicitacao,
        SolicitacaoStatus status,
        Boolean isPF,
        String nomeRazaoSocial,
        String nomeContato,
        String cnpj,
        LocalDateTime ultimaAtualizacao
) {
}
