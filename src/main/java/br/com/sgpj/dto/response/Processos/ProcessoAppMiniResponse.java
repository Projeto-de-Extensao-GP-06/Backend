package br.com.sgpj.dto.response.Processos;

import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.StatusProcesso;

import java.time.LocalDateTime;

public record ProcessoAppMiniResponse(
        Long id,
        Long clienteId,
        String clientNomeRazaoSocial,
        Prioridade prioridade,
        FaseProcesso faseProcesso,
        StatusProcesso status,
        LocalDateTime dataAudiencia,
        SiglaTribunal siglaTribunal,
        String numeroProcesso
) {
}
