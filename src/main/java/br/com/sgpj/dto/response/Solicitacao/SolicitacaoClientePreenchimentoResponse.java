package br.com.sgpj.dto.response.Solicitacao;

import br.com.sgpj.dto.response.Processos.ProcessoApiCreateResponse;
import br.com.sgpj.entity.ComoConheceuEnum;

public record SolicitacaoClientePreenchimentoResponse(
        Long solicitacaoId,
        Long clienteId,
        String nomeRazaoSocial,
        String nomeContato,
        String cnpj,
        String email,
        String telefone,
        String estado,
        String numeroProcesso,
        String descricao,
        ComoConheceuEnum comoConheceu,
        ProcessoApiCreateResponse processo
) {
}
