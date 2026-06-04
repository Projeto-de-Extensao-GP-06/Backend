package br.com.sgpj.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarCargoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 90, message = "Nome deve ter no máximo 90 caracteres")
        String nome
) {}
