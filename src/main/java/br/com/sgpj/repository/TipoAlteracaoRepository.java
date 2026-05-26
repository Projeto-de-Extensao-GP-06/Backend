package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.TipoAlteracao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoAlteracaoRepository extends JpaRepository<TipoAlteracao, Long> {
}
