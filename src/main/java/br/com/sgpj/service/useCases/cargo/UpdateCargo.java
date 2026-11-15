package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.RenomearCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.CargoJaExisteException;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.exception.PermissaoNaoEncontradaException;
import br.com.sgpj.exception.UltimoAdministradorException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.CargoMapper;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.useCases.usuario.AdministradorValidator;
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

    public CargoResponse renomearCargo(Long cargoId, RenomearCargoRequest request) {
        if (ConstantesValidation.CARGO_ADMIN_ID.equals(cargoId)) {
            throw new OperacaoInvalidaException("O cargo 'ADMIN' não pode ser renomeado");
        }
        if (ConstantesValidation.SEM_CARGO_ID.equals(cargoId)) {
            throw new OperacaoInvalidaException("O cargo 'Sem Cargo' não pode ser renomeado");
        }

        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new CargoNaoEncontradoException("Cargo não encontrado: " + cargoId));
        String nome = request.nome().trim();
        if (ConstantesValidation.isNomeCargoReservado(nome)) {
            throw new CargoJaExisteException("O nome " + nome + " é reservado para um cargo do sistema");
        }
        if (cargoRepository.existsByNomeIgnoreCaseAndIdNot(nome, cargoId)) {
            throw new CargoJaExisteException("O cargo " + nome + " já existe");
        }

        cargo.setNome(nome);
        return CargoMapper.toResponse(cargo);
    }

    public CargoResponse atualizarPermissoesCargo(Long cargoId, AtribuirPermissoesRequest request, String emailUsuario){
        if (ConstantesValidation.CARGO_ADMIN_ID.equals(cargoId)) {
            throw new OperacaoInvalidaException("As permissões do cargo 'ADMIN' não podem ser alteradas");
        }
        if (ConstantesValidation.SEM_CARGO_ID.equals(cargoId)) {
            throw new OperacaoInvalidaException("As permissões do cargo 'Sem Cargo' não podem ser alteradas");
        }

        if (!cargoRepository.existsById(cargoId)) {
            throw new CargoNaoEncontradoException("Cargo não encontrado: " + cargoId);
        }
        Cargo cargo = cargoRepository.getReferenceById(cargoId);

        Usuario atribuidoPor = usuarioRepository.findByEmail(emailUsuario);
        if (atribuidoPor == null) {
            log.warn("Usuário com email {} não encontrado.", emailUsuario);
            throw new UsuarioNaoEncontradoException(emailUsuario);
        }

        List<Long> permissaoIds = request.permissaoIds();
        boolean mantemTodasPermissoes = permissaoIds.size() == ConstantesValidation.TOTAL_PERMISSOES_NORMAIS
                || permissaoIds.contains(ConstantesValidation.TODAS_PERMISSOES_ID);

        if (!mantemTodasPermissoes
                && cargoPermissaoRepository.existsById_CargoIdAndId_PermissaoId(cargoId, ConstantesValidation.TODAS_PERMISSOES_ID)) {
            boolean possuiAdminAtivoNoCargo = usuarioRepository.findAllByCargo_Id(cargoId)
                    .stream()
                    .anyMatch(u -> u.isAtivo() && !u.isUsuarioSistema());

            if (possuiAdminAtivoNoCargo
                    && !AdministradorValidator.existeAdministradorAtivoForaDoCargo(usuarioRepository, cargoId)) {
                throw new UltimoAdministradorException(
                        "Não é possível remover as permissões de administrador deste cargo pois ele contém o único administrador ativo do sistema."
                );
            }
        }

        cargoPermissaoRepository.deleteAllById_CargoId(cargoId);

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
