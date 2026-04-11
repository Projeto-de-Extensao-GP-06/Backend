package br.com.sgpj.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UsuarioListarDto(

        @Schema(description = "Id do usuário", example = "1")
        Long id,

        @Schema(description = "Nome do usuário", example = "John Doe")
        String nome,

        @Schema(description = "Email do usuário", example = "john@doe.com")
        String email
) {}
