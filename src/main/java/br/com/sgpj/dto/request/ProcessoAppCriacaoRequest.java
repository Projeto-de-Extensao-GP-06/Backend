package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProcessoAppCriacaoRequest(

        @NotNull(message = "Id do cliente é obrigatório")
        @Schema(description = "Id do cliente associado aquele processo", example = "1")
        Long clienteId,

        @NotBlank(message = "A descrição do caso é obrigatória")
        @Size(min = 10, max = 5000, message = "A descrição do caso deve ter no mínimo 10 caracteres")
        @Schema(description = "Descricao sobre o processo", example = "Esse processo e sobre divisao de bens")
        String descricaoCaso,

        @Size(max = 5000, message = "A observação não pode ter mais de 5000 caracteres")
        @Schema(description = "observacao sobre o processo", example = "Cliente tem algum laudo")
        String observacao,

        @NotNull(message = "Id da prioridade é obrigatório")
        @Schema(description = "Id do nivel de prioridade do processo", example = "1")
        Long prioridadeId,

        @NotNull(message = "Id da fase do processo é obrigatório")
        @Schema(description = "Id da fase do processo", example = "1")
        Long faseProcessoId,

        LocalDateTime dataAudiencia,

        @NotNull(message = "Id da sigla do tribunal é obrigatório")
        @Schema(description = "Id da sigla do tribunal responsavel pelo processo", example = "1")
        Long siglaTribunalId,

        @Pattern(regexp = "^$|^\\d{20}$|^\\d{7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4}$", message = "O CNJ deve conter exatamente 20 dígitos")
        @Schema(description = "Numero do processo para consulta em API", example = "10108901220228260292")
        String numeroProcesso,

        @Positive(message = "O valor da causa deve ser maior que zero")
        @Max(value = 999999999999L, message = "O valor da causa não pode exceder R$ 999.999.999.999,00")
        @Schema(description = "Valor da causa em reais (R$)", example = "2500.00")
        Double valorCausa
) {
}
