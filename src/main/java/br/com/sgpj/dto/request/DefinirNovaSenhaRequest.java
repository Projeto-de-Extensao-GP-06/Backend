package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DefinirNovaSenhaRequest(
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 255)
        @Schema(description = "Senha do usuário", example = "aB1!aaaa")
        String senha
) {}
