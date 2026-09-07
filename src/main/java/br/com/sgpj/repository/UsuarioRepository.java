package br.com.sgpj.repository;

import br.com.sgpj.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

  Usuario findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<Usuario> findFirstByAtivoTrueOrderByIdAsc();

  List<Usuario> findAllByCargoNome(String cargo);

  List<Usuario> findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
          String nome,
          String email
  );

  @Query("""
        SELECT u
        FROM Usuario u
        LEFT JOIN FETCH u.cargo c
        LEFT JOIN FETCH c.permissoes cp
        LEFT JOIN FETCH cp.permissao
        WHERE u.email = :email
        """)
  Optional<Usuario> findByEmailWithPermissoes(
          @Param("email") String email
  );

  boolean existsByCargo_Id(Long cargoId);

  List<Usuario> findAllByCargo_Id(Long cargoId);
}