package br.com.sgpj.dto.response.Solicitacao;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitacaoCandidatoDuplicidadeResponse(
        Long clienteId,
        String nomeRazaoSocial,
        Boolean isPF,
        String statusCliente,
        LocalDateTime dataCadastro,
        List<MotivoDuplicidadeEnum> motivos,
        List<String> contatosCorrespondentes
) {
}
