package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.exception.UltimoAdministradorException;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.useCases.usuario.AdministradorValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteCargo {

    private final CargoRepository cargoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CargoPermissaoRepository cargoPermissaoRepository;

    public DeleteCargo(CargoRepository cargoRepository, UsuarioRepository usuarioRepository, CargoPermissaoRepository cargoPermissaoRepository) {
        this.cargoRepository = cargoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cargoPermissaoRepository = cargoPermissaoRepository;
    }


    public void deletarCargo(Long cargoId){
        if (ConstantesValidation.CARGO_ADMIN_ID.equals(cargoId)) {
            throw new OperacaoInvalidaException("O cargo 'ADMIN' não pode ser excluído");
        }

        if (ConstantesValidation.SEM_CARGO_ID.equals(cargoId)) {
            throw new OperacaoInvalidaException("O cargo 'Sem Cargo' não pode ser excluído");
        }

        if (!cargoRepository.existsById(cargoId)) {
            throw new CargoNaoEncontradoException("Cargo não encontrado: " + cargoId);
        }

        if (usuarioRepository.existsByCargo_Id(cargoId)) {
            List<Usuario> usuariosDoCargo = usuarioRepository.findAllByCargo_Id(cargoId);

            if (cargoPermissaoRepository.existsById_CargoIdAndId_PermissaoId(cargoId, ConstantesValidation.TODAS_PERMISSOES_ID)) {
                boolean possuiAdminAtivoNoCargo = usuariosDoCargo.stream()
                        .anyMatch(u -> u.isAtivo() && !u.isUsuarioSistema());

                if (possuiAdminAtivoNoCargo
                        && !AdministradorValidator.existeAdministradorAtivoForaDoCargo(usuarioRepository, cargoId)) {
                    throw new UltimoAdministradorException(
                            "Não é possível excluir este cargo pois ele contém o único administrador ativo do sistema."
                    );
                }
            }

            Cargo cargoPadrao = cargoRepository.getReferenceById(ConstantesValidation.SEM_CARGO_ID);
            usuariosDoCargo.forEach(usuario -> usuario.setCargo(cargoPadrao));
        }

        cargoPermissaoRepository.deleteAllById_CargoId(cargoId);
        cargoRepository.deleteById(cargoId);
    }

}
