package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProcessoAppCriacaoRequest(

        @NotNull(message = "Id do cliente é obrigatório")
        @Schema(description = "Id do cliente associado aquele processo", example = "1")
        Long clienteId,

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

        @NotNull(message = "Id da fase do processo e obrigatorio")
        @Schema(description = "Id da fase do processo", example = "1")
        Long faseProcessoId,

        LocalDateTime dataAudiencia,

        @NotNull(message = "Id da sigla do tribunal e obrigatorio")
        @Schema(description = "Id da sigla do tribunal responsavel pelo processo", example = "1")
        Long siglaTribunalId,

        @Schema(description = "Numero do processo para consulta em API", example = "10108901220228260292")
        String numeroProcesso,

        @Schema(description = "Valor da causa em reais (R$)", example = "2500.00")
        Double valorCausa
) {
}
