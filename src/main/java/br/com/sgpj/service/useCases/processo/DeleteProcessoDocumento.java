package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteProcessoDocumento {

    private final ProcessoDocumentoRepository processoDocumentoRepository;

    public DeleteProcessoDocumento(ProcessoDocumentoRepository processoDocumentoRepository) {
        this.processoDocumentoRepository = processoDocumentoRepository;
    }

    public void deletarVinculosPorProcessoAppId(Long processoAppId) {
        List<ProcessoDocumento> vinculos = processoDocumentoRepository.findByProcessoAppId(processoAppId);
        if (!vinculos.isEmpty()) {
            processoDocumentoRepository.deleteAll(vinculos);
        }
    }

    public void deletarVinculosPorDocumentoId(Long documentoId) {
        List<ProcessoDocumento> vinculos = processoDocumentoRepository.findByDocumentoId(documentoId);
        if (!vinculos.isEmpty()) {
            processoDocumentoRepository.deleteAll(vinculos);
        }
    }

    public void deletarVinculos(List<ProcessoDocumento> vinculos) {
        if (vinculos != null && !vinculos.isEmpty()) {
            processoDocumentoRepository.deleteAll(vinculos);
        }
    }

    public void desvincularDocumento(Long processoAppId, Long documentoId) {
        br.com.sgpj.entity.Processo.ProcessoDocumentoId id = new br.com.sgpj.entity.Processo.ProcessoDocumentoId(documentoId, processoAppId);
        if (processoDocumentoRepository.existsById(id)) {
            processoDocumentoRepository.deleteById(id);
        }
    }
}
