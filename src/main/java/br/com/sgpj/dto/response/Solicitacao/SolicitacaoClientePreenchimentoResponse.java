package br.com.sgpj.dto.response.Solicitacao;

import br.com.sgpj.dto.response.Processos.ProcessoApiCreateResponse;

public record SolicitacaoClientePreenchimentoResponse(
        Long solicitacaoId,
        Long clienteId,
        String nomeRazaoSocial,
        String nomeContato,
        String email,
        String telefone,
        String estado,
        String numeroProcesso,
        String descricao,
        String comoConheceu,
        ProcessoApiCreateResponse processo
) {
}
