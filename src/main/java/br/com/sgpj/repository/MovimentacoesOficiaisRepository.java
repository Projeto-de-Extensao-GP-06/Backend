package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.MovimentacoesOficiais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimentacoesOficiaisRepository extends JpaRepository<MovimentacoesOficiais, Long> {
}
