package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.Prioridade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrioridadeRepository extends JpaRepository<Prioridade, Long> {

}
