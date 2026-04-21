package br.com.sgpj.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UsuarioListarResponse(

        @Schema(description = "Id do usuário", example = "1")
        Long id,

        @Schema(description = "Nome do usuário", example = "Thauany Bezerra")
        String nome,

        @Schema(description = "Email do usuário", example = "thauany@gmail.com")
        String email,

        @Schema(description = "Id do cargo do usuário", example = "1")
        Long cargoId,

        @Schema(description = "Nome do cargo do usuário", example = "ADMIN")
        String cargoNome,

        @Schema(description = "Data de criação do usuário", example = "2026-04-18")
        LocalDateTime criadoEm,

        @Schema(description = "Indica se o usuário está ativo", example = "true")
        Boolean ativo
) {}
