package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.entity.*;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AtribuirPermissaoEspecial {

    private final CargoPermissaoRepository cargoPermissaoRepository;
    private final PermissaoRepository permissaoRepository;

    public AtribuirPermissaoEspecial(CargoPermissaoRepository cargoPermissaoRepository, PermissaoRepository permissaoRepository) {
        this.cargoPermissaoRepository = cargoPermissaoRepository;
        this.permissaoRepository = permissaoRepository;
    }

    public void execute(Long cargoId, Long permissaoId, Cargo cargo, Usuario atribuidoPor) {
        Permissao permissao = permissaoRepository.getReferenceById(permissaoId);
        cargoPermissaoRepository.save(new CargoPermissao(
                new CargoPermissaoId(cargoId, permissaoId),
                cargo, permissao, atribuidoPor, LocalDateTime.now()
        ));
    }

}
