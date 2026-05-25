package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoProcessoRepository extends JpaRepository<HistoricoProcesso, Long> {
    List<HistoricoProcesso> findByProcessoApp(ProcessoApp processoAppEncontrado);
}
