package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.ProcessoApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessoApiRepository extends JpaRepository<ProcessoApi, Long> {
}