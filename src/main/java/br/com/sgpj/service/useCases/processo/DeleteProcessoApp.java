package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DeleteProcessoApp {

    private final ProcessoAppRepository processoAppRepository;
    private final DeleteProcessoDocumento deleteProcessoDocumento;
    private final DeleteHistoricoProcesso deleteHistoricoProcesso;

    public DeleteProcessoApp(
            ProcessoAppRepository processoAppRepository,
            DeleteProcessoDocumento deleteProcessoDocumento,
            DeleteHistoricoProcesso deleteHistoricoProcesso
    ) {
        this.processoAppRepository = processoAppRepository;
        this.deleteProcessoDocumento = deleteProcessoDocumento;
        this.deleteHistoricoProcesso = deleteHistoricoProcesso;
    }

    @Transactional
    public void deletarProcesso(Long id) {
        ProcessoApp processo = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id));

        // 1. Remove os vínculos com documentos para não violar foreign key constraint
        deleteProcessoDocumento.deletarVinculosPorProcessoAppId(id);

        // 2. Remove o histórico de alterações vinculado ao processo
        deleteHistoricoProcesso.deletarPorProcessoApp(processo);

        // 3. Remove o processo (ProcessoApi vinculado é removido via cascade JPA)
        processoAppRepository.delete(processo);
    }

    @Transactional
    public void deletarProcessos(List<ProcessoApp> processos) {
        if (processos != null && !processos.isEmpty()) {
            for (ProcessoApp processo : processos) {
                if (processo.getId() != null) {
                    deleteProcessoDocumento.deletarVinculosPorProcessoAppId(processo.getId());
                    deleteHistoricoProcesso.deletarPorProcessoApp(processo);
                }
            }
            processoAppRepository.deleteAll(processos);
        }
    }
}
