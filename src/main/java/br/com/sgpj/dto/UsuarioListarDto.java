package br.com.sgpj.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioListarDto(

        @Schema(description = "Id do usuário", example = "1")
        Long id,

        @Schema(description = "Nome do usuário", example = "John Doe")
        String nome,

        @Schema(description = "Email do usuário", example = "john@doe.com")
        String email,

        @Schema(description = "Id do cargo do usuário", example = "2")
        Long cargoId,

        @Schema(description = "Nome do cargo do usuário", example = "Administrador")
        String cargoNome,

        @Schema(description = "Data de criação do usuário", example = "2026-04-18")
        LocalDateTime criadoEm,

        @Schema(description = "Indica se o usuário está ativo", example = "true")
        Boolean ativo
) {}
