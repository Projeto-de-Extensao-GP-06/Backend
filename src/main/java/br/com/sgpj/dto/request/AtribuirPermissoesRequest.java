package br.com.sgpj.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AtribuirPermissoesRequest(
        @NotEmpty(message = "Informe ao menos uma permissão")
        List<Long> permissaoIds
) {
}
