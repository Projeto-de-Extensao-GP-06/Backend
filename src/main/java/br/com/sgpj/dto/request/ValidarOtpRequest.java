package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ValidarOtpRequest(
        @NotBlank(message = "E-mail é obrigatório")
        @Email
        @Schema(description = "Email do usuário", example = "john@doe.com")
        String email,

        @NotBlank(message = "OTP é obrigatória")
        @Size(min = 6, max = 6)
        @Schema(description = "OTP enviada por email", example = "123456")
        String otp
) {}
