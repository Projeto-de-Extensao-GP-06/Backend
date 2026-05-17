package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCriacaoRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 100)
        @Schema(description = "Nome do usuário", example = "John Doe")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email
        @Schema(description = "Email do usuário", example = "john@doe.com")
        String email,

        @NotNull(message = "Id do cargo é obrigatório")
        @Schema(description = "Id do cargo", example = "1")
        Long cargoId
) {}
