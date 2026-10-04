package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.TipoAlteracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoAlteracaoRepository extends JpaRepository<TipoAlteracao, Long> {
    Optional<TipoAlteracao> findByNomeIgnoreCase(String nome);
}
