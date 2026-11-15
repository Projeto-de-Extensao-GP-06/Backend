package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.useCases.cargo.ConstantesValidation;

import java.util.List;

public final class AdministradorValidator {

    private AdministradorValidator() {}

    public static boolean isCargoAdministrador(Cargo cargo) {
        if (cargo == null) {
            return false;
        }
        if (ConstantesValidation.CARGO_ADMIN_ID.equals(cargo.getId())) {
            return true;
        }
        if (cargo.getNome() != null
                && ConstantesValidation.CARGO_ADMIN_NOME.equalsIgnoreCase(cargo.getNome().trim())) {
            return true;
        }
        if (cargo.getPermissoes() != null) {
            return cargo.getPermissoes().stream()
                    .anyMatch(cp -> cp.getPermissao() != null && (
                            ConstantesValidation.TODAS_PERMISSOES_ID.equals(cp.getPermissao().getId())
                                    || "TODAS AS PERMISSÕES".equalsIgnoreCase(cp.getPermissao().getNome())
                    ));
        }
        return false;
    }

    public static boolean isUsuarioAdministradorAtivo(Usuario usuario) {
        return usuario != null
                && usuario.isAtivo()
                && !usuario.isUsuarioSistema()
                && isCargoAdministrador(usuario.getCargo());
    }

    public static boolean existeOutroAdministradorAtivo(
            UsuarioRepository usuarioRepository,
            Long usuarioIdIgnorado
    ) {
        List<Usuario> todos = usuarioRepository.findAll();
        return todos.stream()
                .anyMatch(u -> u.getId() != null
                        && !u.getId().equals(usuarioIdIgnorado)
                        && isUsuarioAdministradorAtivo(u));
    }

    public static boolean existeAdministradorAtivoForaDoCargo(
            UsuarioRepository usuarioRepository,
            Long cargoIdIgnorado
    ) {
        List<Usuario> todos = usuarioRepository.findAll();
        return todos.stream()
                .anyMatch(u -> isUsuarioAdministradorAtivo(u)
                        && u.getCargo() != null
                        && !cargoIdIgnorado.equals(u.getCargo().getId()));
    }
}
