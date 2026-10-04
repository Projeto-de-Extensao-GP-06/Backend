package br.com.sgpj.dto.request;

import br.com.sgpj.entity.Processo.StatusProcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProcessoAppUpdateRequest(

        @Schema(description = "Número de CNJ do processo", example = "10108901220228260292")
        String numeroCnj,

        @NotBlank(message = "Descricao do caso e obrigatoria")
        @Size(min = 10, max = 5000)
        @Schema(description = "Descricao sobre o processo", example = "Esse processo e sobre divisao de bens")
        String descricaoCaso,

        @Size(min = 10, max = 5000)
        @Schema(description = "observacao sobre o processo", example = "Cliente tem algum laudo")
        String observacao,

        @NotNull(message = "Id da prioridade e obrigatorio")
        @Schema(description = "Id do nivel de prioridade do processo", example = "1")
        Long prioridadeId,

        @NotNull(message = "O status do processo é obrigatório")
        @Schema(description = "Status do processo", example = "ATIVO", allowableValues = {"ATIVO", "SUSPENSO", "ENCERRADO"})
        StatusProcesso status,

        @NotNull(message = "Id da fase do processo e obrigatorio")
        @Schema(description = "Id da fase do processo", example = "1")
        Long faseProcessoId,

        @Schema(description = "Id da sigla do tribunal", example = "1")
        Long siglaTribunalId,

        LocalDateTime dataAudiencia,

        @Schema(description = "Valor da causa em reais (R$)", example = "2500.00")
        Double valorCausa,

        @Schema(description = "Id do motivo de suspensão ou encerramento. Obrigatório quando status for SUSPENSO ou ENCERRADO.", example = "1")
        Integer motivoStatusProcessoId,

        @Schema(description = "Justificativa complementar (opcional)", example = "Aguardando documentação do cliente")
        String justificativa
) {
    public ProcessoAppUpdateRequest(
            String numeroCnj,
            String descricaoCaso,
            String observacao,
            Long prioridadeId,
            StatusProcesso status,
            Long faseProcessoId,
            LocalDateTime dataAudiencia,
            Double valorCausa,
            Integer motivoStatusProcessoId,
            String justificativa
    ) {
        this(numeroCnj, descricaoCaso, observacao, prioridadeId, status, faseProcessoId, null,
                dataAudiencia, valorCausa, motivoStatusProcessoId, justificativa);
    }
}
