package br.com.sgpj.repository;

import br.com.sgpj.entity.CargoPermissao;
import br.com.sgpj.entity.CargoPermissaoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoPermissaoRepository extends JpaRepository<CargoPermissao, CargoPermissaoId> {

    boolean existsById_CargoIdAndId_PermissaoId(Long cargoId, Long permissaoId);

    List<CargoPermissao> findAllById_CargoId(Long cargoId);

    void deleteAllById_CargoId(Long cargoId);

    void deleteById_CargoIdAndId_PermissaoId(Long cargoId, Long permissaoId);
}
