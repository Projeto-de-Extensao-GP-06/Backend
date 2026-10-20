package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.FaseProcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaseProcessoRepository extends JpaRepository<FaseProcesso, Long> {
	Optional<FaseProcesso> findByNomeIgnoreCase(String nome);
}
