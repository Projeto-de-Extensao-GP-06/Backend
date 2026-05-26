package br.com.sgpj.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TipoDocumentoResponse(
        @Schema(description = "Id do tipo de documento", example = "1")
        Long id,

        @Schema(description = "Nome do tipo de documento", example = "Contrato")
        String nome
) {}
