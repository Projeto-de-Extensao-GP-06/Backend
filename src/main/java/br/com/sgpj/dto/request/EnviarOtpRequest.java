package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EnviarOtpRequest(
        @NotBlank(message = "E-mail é obrigatório")
        @Email
        @Schema(description = "Email do usuário", example = "john@doe.com")
        String email
) {}
