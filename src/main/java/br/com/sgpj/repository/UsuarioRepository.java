package br.com.sgpj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.sgpj.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

  Optional<Usuario> findByEmail(String email);

  Optional<Usuario> findFirstByAtivoTrueOrderByIdAsc();

  boolean existsByEmail(String email);

  List<Usuario> findByCargo_NomeIgnoreCase(String funcao);

  List<Usuario> findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(String nome, String email);

}
