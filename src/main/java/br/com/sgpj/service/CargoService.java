package br.com.sgpj.service;

import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.CargoJaExisteException;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.mapper.CargoMapper;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CargoService {

    private final CargoRepository cargoRepository;
    private final PermissaoRepository permissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CargoPermissaoRepository cargoPermissaoRepository;

    public CargoService(CargoRepository cargoRepository, PermissaoRepository permissaoRepository, UsuarioRepository usuarioRepository, CargoPermissaoRepository cargoPermissaoRepository) {
        this.cargoRepository = cargoRepository;
        this.permissaoRepository = permissaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cargoPermissaoRepository = cargoPermissaoRepository;
    }



    private static final Long CARGO_ADMIN_ID = 1L;
    private static final Long SEM_CARGO_ID = 2L;
    private static final Long TODAS_PERMISSOES_ID = 18L;
    private static final Long SEM_PERMISSOES_ID = 19L;
    private static final int TOTAL_PERMISSOES_NORMAIS = 17;

    @Transactional(readOnly = true)
    public Page<CargoResponse> listarPaginado(Pageable pageable) {
        Page<Long> idsPage = cargoRepository.findAllIds(pageable);
        List<Cargo> cargos = cargoRepository.findAllWithPermissoesByIds(idsPage.getContent());

        Map<Long, Cargo> cargoMap = cargos.stream()
                .collect(Collectors.toMap(Cargo::getId, c -> c));

        List<CargoResponse> content = idsPage.getContent().stream()
                .map(id -> CargoMapper.toResponse(cargoMap.get(id)))
                .toList();

        return new PageImpl<>(content, pageable, idsPage.getTotalElements());
    }

    @Transactional
    public CargoResponse criar(CriarCargoRequest request, String emailUsuario) {
        if (cargoRepository.existsByNome(request.nome())) {
            throw new CargoJaExisteException("O cargo " + request.nome() + " já existe");
        }

        Usuario criadoPor = usuarioRepository.findByEmail(emailUsuario);
        Permissao semPermissoes = permissaoRepository.getReferenceById(SEM_PERMISSOES_ID);

        Cargo cargo = new Cargo();
        cargo.setNome(request.nome());
        cargo.setCriadoPor(criadoPor);
        cargo.setCriadoEm(LocalDateTime.now());

        Cargo cargoSalvo = cargoRepository.save(cargo);

        cargoPermissaoRepository.save(new CargoPermissao(
                new CargoPermissaoId(cargoSalvo.getId(), SEM_PERMISSOES_ID),
                cargoSalvo,
                semPermissoes,
                criadoPor,
                LocalDateTime.now()
        ));

        return new CargoResponse(cargoSalvo.getId(), cargoSalvo.getNome(),
                List.of(new PermissaoResponse(semPermissoes.getId(), semPermissoes.getNome())));
    }

    @Transactional
    public CargoResponse atualizarPermissoes(Long cargoId,
                                             AtribuirPermissoesRequest request,
                                             String emailUsuario) {

        Cargo cargo = cargoRepository.getReferenceById(cargoId);
        Usuario atribuidoPor = usuarioRepository.findByEmail(emailUsuario);

        cargoPermissaoRepository.deleteAllById_CargoId(cargoId);

        List<Long> permissaoIds = request.permissaoIds();

        if (permissaoIds.isEmpty()) {
            atribuirPermissaoEspecial(cargoId, SEM_PERMISSOES_ID, cargo, atribuidoPor);

        } else if (permissaoIds.size() == TOTAL_PERMISSOES_NORMAIS) {
            atribuirPermissaoEspecial(cargoId, TODAS_PERMISSOES_ID, cargo, atribuidoPor);

        } else {
            for (Long permissaoId : permissaoIds) {
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

    @Transactional
    public void deletar(Long cargoId) {
        if (cargoId.equals(CARGO_ADMIN_ID)) {
            throw new OperacaoInvalidaException("O cargo 'ADMIN' não pode ser excluído");
        }

        if (cargoId.equals(SEM_CARGO_ID)) {
            throw new OperacaoInvalidaException("O cargo 'Sem Cargo' não pode ser excluído");
        }

        if (!cargoRepository.existsById(cargoId)) {
            throw new CargoNaoEncontradoException("Cargo não encontrado: " + cargoId);
        }

        if (usuarioRepository.existsByCargo_Id(cargoId)) {
            Cargo cargoPadrao = cargoRepository.getReferenceById(SEM_CARGO_ID);

            usuarioRepository.findAllByCargo_Id(cargoId)
                    .forEach(usuario -> usuario.setCargo(cargoPadrao));
        }

        cargoPermissaoRepository.deleteAllById_CargoId(cargoId);
        cargoRepository.deleteById(cargoId);
    }

    private void atribuirPermissaoEspecial(Long cargoId, Long permissaoId, Cargo cargo, Usuario atribuidoPor) {
        Permissao permissao = permissaoRepository.getReferenceById(permissaoId);
        cargoPermissaoRepository.save(new CargoPermissao(
                new CargoPermissaoId(cargoId, permissaoId),
                cargo, permissao, atribuidoPor, LocalDateTime.now()
        ));
    }
}
