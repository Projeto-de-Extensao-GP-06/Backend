package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessoDocumentoRepository extends JpaRepository<ProcessoDocumento, ProcessoDocumentoId> {
    List<ProcessoDocumento> findByProcessoAppId(Long processoAppId);
    List<ProcessoDocumento> findByDocumentoId(Long documentoId);
}
