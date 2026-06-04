package br.com.sgpj.service;

import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.CargoMapper;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository cargoRepository;
    private final CargoPermissaoRepository cargoPermissaoRepository;
    private final PermissaoRepository permissaoRepository;
    private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;

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
        Usuario criadoPor = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(emailUsuario);

        Cargo cargo = new Cargo();
        cargo.setNome(request.nome());
        cargo.setCriadoPor(criadoPor);
        cargo.setCriadoEm(LocalDateTime.now());

        Cargo cargoSalvo = cargoRepository.save(cargo);

        return new CargoResponse(cargoSalvo.getId(), cargoSalvo.getNome(), List.of());
    }

}
