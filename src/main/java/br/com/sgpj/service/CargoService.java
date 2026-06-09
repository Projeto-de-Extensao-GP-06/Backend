package br.com.sgpj.service;

import br.com.sgpj.adapter.database.CargoPermissaoAdapter;
import br.com.sgpj.adapter.database.CargoPersistenciaAdapter;
import br.com.sgpj.adapter.database.PermissaoPersistenciaAdapter;
import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.CargoJaExisteException;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.mapper.CargoMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CargoService {

    private final CargoPersistenciaAdapter cargoPersistenciaAdapter;
    private final PermissaoPersistenciaAdapter permissaoPersistenciaAdapter;
    private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
    private final CargoPermissaoAdapter cargoPermissaoAdapter;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    private static final Long CARGO_ADMIN_ID = 1L;
    private static final Long SEM_CARGO_ID = 2L;
    private static final Long TODAS_PERMISSOES_ID = 18L;
    private static final Long SEM_PERMISSOES_ID = 19L;
    private static final int TOTAL_PERMISSOES_NORMAIS = 17;

    @Transactional(readOnly = true)
    public Page<CargoResponse> listarPaginado(Pageable pageable) {
        Page<Long> idsPage = cargoPersistenciaAdapter.findAllIds(pageable);
        List<Cargo> cargos = cargoPersistenciaAdapter.findAllWithPermissoesByIds(idsPage);

        Map<Long, Cargo> cargoMap = cargos.stream()
                .collect(Collectors.toMap(Cargo::getId, c -> c));

        List<CargoResponse> content = idsPage.getContent().stream()
                .map(id -> CargoMapper.toResponse(cargoMap.get(id)))
                .toList();

        return new PageImpl<>(content, pageable, idsPage.getTotalElements());
    }

    @Transactional
    public CargoResponse criar(CriarCargoRequest request, String emailUsuario) {
        if (cargoPersistenciaAdapter.existsByNome(request.nome())) {
            throw new CargoJaExisteException("O cargo " + request.nome() + " já existe");
        }

        Usuario criadoPor = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(emailUsuario);
        Permissao semPermissoes = permissaoPersistenciaAdapter.buscarPermissaoPorId(SEM_PERMISSOES_ID);

        Cargo cargo = new Cargo();
        cargo.setNome(request.nome());
        cargo.setCriadoPor(criadoPor);
        cargo.setCriadoEm(LocalDateTime.now());

        Cargo cargoSalvo = cargoPersistenciaAdapter.salvar(cargo);

        cargoPermissaoAdapter.salvar(new CargoPermissao(
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

        Cargo cargo = cargoPersistenciaAdapter.buscarCargoPorId(cargoId);
        Usuario atribuidoPor = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(emailUsuario);

        cargoPermissaoAdapter.deleteAllById_CargoId(cargoId);

        List<Long> permissaoIds = request.permissaoIds();

        if (permissaoIds.isEmpty()) {
            atribuirPermissaoEspecial(cargoId, SEM_PERMISSOES_ID, cargo, atribuidoPor);

        } else if (permissaoIds.size() == TOTAL_PERMISSOES_NORMAIS) {
            atribuirPermissaoEspecial(cargoId, TODAS_PERMISSOES_ID, cargo, atribuidoPor);

        } else {
            for (Long permissaoId : permissaoIds) {
                Permissao permissao = permissaoPersistenciaAdapter.buscarPermissaoPorId(permissaoId);
                cargoPermissaoAdapter.salvar(new CargoPermissao(
                        new CargoPermissaoId(cargoId, permissaoId),
                        cargo, permissao, atribuidoPor, LocalDateTime.now()
                ));
            }
        }

        List<CargoPermissao> permissoes = cargoPermissaoAdapter.findAllById_CargoId(cargoId);
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

        if (!cargoPersistenciaAdapter.existsById(cargoId)) {
            throw new CargoNaoEncontradoException("Cargo não encontrado: " + cargoId);
        }

        if (usuarioPersistenciaAdapter.existsByCargo(cargoId)) {
            Cargo cargoPadrao = cargoPersistenciaAdapter.buscarCargoPorId(SEM_CARGO_ID);

            usuarioPersistenciaAdapter.findAllByCargo_Id(cargoId)
                    .forEach(usuario -> usuario.setCargo(cargoPadrao));
        }

        cargoPermissaoAdapter.deleteAllById_CargoId(cargoId);
        cargoPersistenciaAdapter.deleteById(cargoId);
    }

    public boolean isCorrectScope(HttpServletRequest httpRequest, String scope) {
        return usuarioAutenticacaoAdapter.isCorrectScope(httpRequest, scope);
    }

    private void atribuirPermissaoEspecial(Long cargoId, Long permissaoId, Cargo cargo, Usuario atribuidoPor) {
        Permissao permissao = permissaoPersistenciaAdapter.buscarPermissaoPorId(permissaoId);
        cargoPermissaoAdapter.salvar(new CargoPermissao(
                new CargoPermissaoId(cargoId, permissaoId),
                cargo, permissao, atribuidoPor, LocalDateTime.now()
        ));
    }
}
