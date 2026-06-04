package br.com.sgpj.dto.response;

import java.util.List;

public record CargoResponse(
        Long cargoId,
        String nome,
        List<PermissaoResponse> permissoes
) {
}
