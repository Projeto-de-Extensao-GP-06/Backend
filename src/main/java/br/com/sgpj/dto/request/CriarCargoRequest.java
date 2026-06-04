package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarCargoRequest(
        @Schema(description = "Nome do cargo a ser criado", example = "Advogado")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 90, message = "Nome deve ter no máximo 90 caracteres")
        String nome
) {}
