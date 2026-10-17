package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioAlterarSenhaRequest(
        @NotBlank(message = "A senha atual é obrigatória")
        @Schema(description = "Senha atual do usuário", example = "SenhaAntiga@123")
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 8, max = 255, message = "A nova senha deve ter entre 8 e 255 caracteres")
        @Schema(description = "Nova senha do usuário", example = "NovaSenha@123")
        String novaSenha
) {}
