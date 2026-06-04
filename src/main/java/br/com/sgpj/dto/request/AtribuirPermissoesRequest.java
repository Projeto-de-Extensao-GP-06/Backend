package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AtribuirPermissoesRequest(
        @Schema(description = "Lista de IDs das permissões que serão atribuídas ao cargo", example = "[1, 2, 3]")
        @NotEmpty(message = "Informe ao menos uma permissão")
        List<Long> permissaoIds
) {
}
