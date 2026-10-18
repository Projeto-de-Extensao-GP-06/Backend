package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.Prioridade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrioridadeRepository extends JpaRepository<Prioridade, Long> {
	Optional<Prioridade> findByNomeIgnoreCase(String nome);
}
