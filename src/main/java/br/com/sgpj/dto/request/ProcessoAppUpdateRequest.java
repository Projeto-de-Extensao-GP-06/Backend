package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProcessoAppUpdateRequest(

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

        @NotNull(message = "O processo deve estar ativo ou inativo")
        Boolean ativo,

        @NotNull(message = "Id da fase do processo e obrigatorio")
        @Schema(description = "Id da fase do processo", example = "1")
        Long faseProcessoId,

        LocalDateTime dataAudiencia,

        @Schema(description = "Valor da causa em reais (R$)", example = "2500.00")
        Double valorCausa
) {
}
