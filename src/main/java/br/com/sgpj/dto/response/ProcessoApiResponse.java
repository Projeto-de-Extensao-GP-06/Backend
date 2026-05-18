package br.com.sgpj.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProcessoApiResponse(
        Long id,
        String numeroCnj,
        String tribunalTramitacao,
        String observacao,
        String instancia,
        LocalDateTime dataAjuizamento,
        Integer nivelSigilo,
        String classeProcessual,
        String formato,
        String sistemaTribunal,
        String orgaoJulgador,
        String assunto,
        List<MovimentacaoOficialResponse> movimentacoes
) {
}
