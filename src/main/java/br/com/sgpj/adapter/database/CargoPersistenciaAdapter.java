package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CargoPersistenciaAdapter {

    private final CargoRepository cargoRepository;

    public Cargo buscarCargoPorId(Long id) {
        return cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNaoEncontradoException("Cargo não encontrado: " + id));
    }

    public Cargo buscarPorNome(String nomeCargo) {
        return cargoRepository.findByNome(nomeCargo)
                .orElseThrow(() -> new CargoNaoEncontradoException("Cargo não encontrado: " + nomeCargo));
    }

    public Cargo salvar(Cargo cargo) {
        return cargoRepository.save(cargo);
    }

    public Page<Long> findAllIds(Pageable pageable) {
        return cargoRepository.findAllIds(pageable);
    }

    public List<Cargo> findAllWithPermissoesByIds(Page<Long> idsPage) { return cargoRepository.findAllWithPermissoesByIds(idsPage.getContent()); }

    public void deleteById(Long cargoId) { cargoRepository.deleteById(cargoId); }

    public boolean existsById(Long cargoId) { return cargoRepository.existsById(cargoId); }

    public boolean existsByNome(String nomeCargo) { return cargoRepository.existsByNome(nomeCargo); }
}
