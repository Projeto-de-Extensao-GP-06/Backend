package br.com.sgpj.service;

import br.com.sgpj.adapter.database.CargoPermissaoAdapter;
import br.com.sgpj.adapter.database.CargoPersistenciaAdapter;
import br.com.sgpj.adapter.database.PermissaoPersistenciaAdapter;
import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.mapper.CargoMapper;
import br.com.sgpj.repository.PermissaoRepository;
import jakarta.persistence.EntityNotFoundException;
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
        Usuario criadoPor = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(emailUsuario);

        Cargo cargo = new Cargo();
        cargo.setNome(request.nome());
        cargo.setCriadoPor(criadoPor);
        cargo.setCriadoEm(LocalDateTime.now());

        Cargo cargoSalvo = cargoPersistenciaAdapter.salvar(cargo);

        return new CargoResponse(cargoSalvo.getId(), cargoSalvo.getNome(), List.of());
    }

    @Transactional
    public CargoResponse atualizarPermissoes(Long cargoId,
                                            AtribuirPermissoesRequest request,
                                            String emailUsuario) {

        Cargo cargo = cargoPersistenciaAdapter.buscarCargoPorId(cargoId);
        Usuario atribuidoPor = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(emailUsuario);
        cargoPermissaoAdapter.deleteAllById_CargoId(cargoId);

        for (Long permissaoId : request.permissaoIds()) {
            Permissao permissao = permissaoPersistenciaAdapter.buscarPermissaoPorId(permissaoId);

            cargoPermissaoAdapter.salvar(new CargoPermissao(
                    new CargoPermissaoId(cargoId, permissaoId),
                    cargo,
                    permissao,
                    atribuidoPor,
                    LocalDateTime.now()
            ));
        }

        // Recarrega com as permissões atualizadas
        List<CargoPermissao> permissoes = cargoPermissaoAdapter.findAllById_CargoId(cargoId);
        List<PermissaoResponse> permissoesResponse = permissoes.stream()
                .map(cp -> new PermissaoResponse(cp.getPermissao().getId(), cp.getPermissao().getNome()))
                .toList();

        return new CargoResponse(cargo.getId(), cargo.getNome(), permissoesResponse);
    }
}
