package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.entity.Cargo;

import java.util.List;

public class CargoMapper {

    public static CargoResponse toResponse(Cargo cargo) {
        List<PermissaoResponse> permissoes = cargo.getPermissoes().stream()
                .map(cp -> new PermissaoResponse(
                        cp.getPermissao().getId(),
                        cp.getPermissao().getNome()
                ))
                .toList();

        return new CargoResponse(cargo.getId(), cargo.getNome(), permissoes);
    }
}
