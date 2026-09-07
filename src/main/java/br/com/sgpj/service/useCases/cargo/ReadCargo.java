package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.mapper.CargoMapper;
import br.com.sgpj.repository.CargoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReadCargo {

    private final CargoRepository cargoRepository;

    public ReadCargo(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

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
}
