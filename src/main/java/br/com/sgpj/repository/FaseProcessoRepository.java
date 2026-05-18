package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.FaseProcesso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaseProcessoRepository extends JpaRepository<FaseProcesso, Long> {
}
