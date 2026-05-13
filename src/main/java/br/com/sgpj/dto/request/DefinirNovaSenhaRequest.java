package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DefinirNovaSenhaRequest(
        @NotBlank(message = "E-mail é obrigatório")
        @Email
        @Schema(description = "Email do usuário", example = "john@doe.com")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 20)
        @Schema(description = "Senha do usuário", example = "123456")
        String senha
) {}
