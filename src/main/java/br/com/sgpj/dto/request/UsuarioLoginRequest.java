package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioLoginRequest(

        @NotBlank(message = "E-mail é obrigatório")
        @Email
        @Schema(description = "E-mail do usuário", example = "thauany@gmail.com")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Schema(description = "Senha do usuário", example = "123456")
        String senha
) {}
