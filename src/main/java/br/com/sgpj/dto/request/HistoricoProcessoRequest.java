package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record HistoricoProcessoRequest(

        @NotNull(message = "Id do processo responsavel e obrigatorio")
        @Schema(description = "Id do processp responsavel", example = "1")
        Long processoAppId,
        String descricaoAlteracao,
        LocalDateTime dataHoraAlteracao,
        @NotNull(message = "Id do tipo de alteracao  e obrigatorio")
        @Schema(description = "Id do tipo de alteracao", example = "1")
        Long tipoAlteracaoId,
        @Schema(description = "Id da fase anterior", example = "null")
        Long faseAnteriorId,
        @Schema(description = "Id da fase nova, quando a alteração for de fase", example = "1")
        Long faseNovaId

) {
}
