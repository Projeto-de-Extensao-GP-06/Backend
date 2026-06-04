package br.com.sgpj.repository;

import br.com.sgpj.entity.Cargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CargoRepository extends JpaRepository<Cargo, Long> {

    @Query("SELECT c.id FROM Cargo c ORDER BY c.nome ASC")
    Page<Long> findAllIds(Pageable pageable);

    @Query("""
        SELECT DISTINCT c FROM Cargo c
        LEFT JOIN FETCH c.permissoes cp
        LEFT JOIN FETCH cp.permissao
        WHERE c.id IN :ids
        """)
    List<Cargo> findAllWithPermissoesByIds(@Param("ids") List<Long> ids);

    Optional<Cargo> findByNome(String nomeCargo);
}
