package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.PermissaoNaoEncontradaException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.CargoMapper;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class UpdateCargo {

    private final CargoRepository cargoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CargoPermissaoRepository cargoPermissaoRepository;
    private final PermissaoRepository permissaoRepository;
    private final AtribuirPermissaoEspecial atribuirPermissaoEspecial;

    public UpdateCargo(CargoRepository cargoRepository, UsuarioRepository usuarioRepository, CargoPermissaoRepository cargoPermissaoRepository, PermissaoRepository permissaoRepository, AtribuirPermissaoEspecial atribuirPermissaoEspecial) {
        this.cargoRepository = cargoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cargoPermissaoRepository = cargoPermissaoRepository;
        this.permissaoRepository = permissaoRepository;
        this.atribuirPermissaoEspecial = atribuirPermissaoEspecial;
    }

    public CargoResponse atualizarPermissoesCargo(Long cargoId, AtribuirPermissoesRequest request, String emailUsuario){
        if (!cargoRepository.existsById(cargoId)) {
            throw new CargoNaoEncontradoException("Cargo não encontrado: " + cargoId);
        }
        Cargo cargo = cargoRepository.getReferenceById(cargoId);

        Usuario atribuidoPor = usuarioRepository.findByEmail(emailUsuario);
        if (atribuidoPor == null) {
            log.warn("Usuário com email {} não encontrado.", emailUsuario);
            throw new UsuarioNaoEncontradoException(emailUsuario);
        }

        cargoPermissaoRepository.deleteAllById_CargoId(cargoId);

        List<Long> permissaoIds = request.permissaoIds();

        if (permissaoIds.isEmpty()) {
            atribuirPermissaoEspecial.execute(cargoId, ConstantesValidation.SEM_PERMISSOES_ID, cargo, atribuidoPor);

        } else if (permissaoIds.size() == ConstantesValidation.TOTAL_PERMISSOES_NORMAIS) {
            atribuirPermissaoEspecial.execute(cargoId, ConstantesValidation.TODAS_PERMISSOES_ID, cargo, atribuidoPor);

        } else {
            for (Long permissaoId : permissaoIds) {
                if (!permissaoRepository.existsById(permissaoId)) {
                    throw new PermissaoNaoEncontradaException("Permissão não encontrada: " + permissaoId);
                }
                Permissao permissao = permissaoRepository.getReferenceById(permissaoId);
                cargoPermissaoRepository.save(new CargoPermissao(
                        new CargoPermissaoId(cargoId, permissaoId),
                        cargo, permissao, atribuidoPor, LocalDateTime.now()
                ));
            }
        }

        List<CargoPermissao> permissoes = cargoPermissaoRepository.findAllById_CargoId(cargoId);
        cargo.setPermissoes(permissoes);

        return CargoMapper.toResponse(cargo);
    }

}
