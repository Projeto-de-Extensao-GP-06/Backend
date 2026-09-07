package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

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
        if (cargoId.equals(ConstantesValidation.CARGO_ADMIN_ID)) {
            throw new OperacaoInvalidaException("O cargo 'ADMIN' não pode ser excluído");
        }

        if (cargoId.equals(ConstantesValidation.SEM_CARGO_ID)) {
            throw new OperacaoInvalidaException("O cargo 'Sem Cargo' não pode ser excluído");
        }

        if (!cargoRepository.existsById(cargoId)) {
            throw new CargoNaoEncontradoException("Cargo não encontrado: " + cargoId);
        }

        if (usuarioRepository.existsByCargo_Id(cargoId)) {
            Cargo cargoPadrao = cargoRepository.getReferenceById(ConstantesValidation.SEM_CARGO_ID);

            usuarioRepository.findAllByCargo_Id(cargoId)
                    .forEach(usuario -> usuario.setCargo(cargoPadrao));
        }

        cargoPermissaoRepository.deleteAllById_CargoId(cargoId);
        cargoRepository.deleteById(cargoId);
    }

}
