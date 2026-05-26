package br.com.sgpj.dto.response.Processos;

import br.com.sgpj.dto.response.MovimentacaoOficialResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProcessoApiResponse(
        Long id,
        String numeroCnj,
        String tribunalTramitacao,
        String instancia,
        LocalDateTime dataAjuizamento,
        Integer nivelSigilo,
        String classeProcessual,
        String formato,
        String sistemaTribunal,
        String orgaoJulgador,
        String assunto,
        MovimentacaoOficialResponse ultimoMovimento
) {
}
