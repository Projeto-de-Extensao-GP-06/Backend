package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.ProcessoApp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessoAppRepository extends JpaRepository<ProcessoApp, Long> {
}
