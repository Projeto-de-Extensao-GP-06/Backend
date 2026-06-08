package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.CargoPermissao;
import br.com.sgpj.repository.CargoPermissaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CargoPermissaoAdapter {

    private final CargoPermissaoRepository cargoPermissaoRepository;

    public CargoPermissao salvar(CargoPermissao cargoPermissao) {
        return cargoPermissaoRepository.save(cargoPermissao);
    }

    public void deleteAllById_CargoId(Long cargoId) {
        cargoPermissaoRepository.deleteAllById_CargoId(cargoId);
    }

    public List<CargoPermissao> findAllById_CargoId(Long cargoId) {
        return cargoPermissaoRepository.findAllById_CargoId(cargoId);
    }
}
