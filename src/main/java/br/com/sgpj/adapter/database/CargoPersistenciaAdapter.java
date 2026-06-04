package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.repository.CargoRepository;
import jakarta.persistence.EntityNotFoundException;
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

    public Cargo salvar(Cargo cargo) {
        return cargoRepository.save(cargo);
    }

    public Page<Long> findAllIds(Pageable pageable) {
        return cargoRepository.findAllIds(pageable);
    }

    public List<Cargo> findAllWithPermissoesByIds(Page<Long> idsPage) {
        return cargoRepository.findAllWithPermissoesByIds(idsPage.getContent());
    }
}
