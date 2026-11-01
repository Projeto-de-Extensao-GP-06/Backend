package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.SiglaTribunal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiglaTribunalRepository extends JpaRepository<SiglaTribunal, Long> {
    Optional<SiglaTribunal> findByNomeIgnoreCase(String nome);
}
