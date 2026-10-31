package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.service.useCases.cliente.AtualizarStatusClientePorProcessos;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DeleteProcessoApp {

    private final ProcessoAppRepository processoAppRepository;
    private final DeleteProcessoDocumento deleteProcessoDocumento;
    private final DeleteHistoricoProcesso deleteHistoricoProcesso;
    private final AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos;

    public DeleteProcessoApp(
            ProcessoAppRepository processoAppRepository,
            DeleteProcessoDocumento deleteProcessoDocumento,
            DeleteHistoricoProcesso deleteHistoricoProcesso,
            AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos
    ) {
        this.processoAppRepository = processoAppRepository;
        this.deleteProcessoDocumento = deleteProcessoDocumento;
        this.deleteHistoricoProcesso = deleteHistoricoProcesso;
        this.atualizarStatusClientePorProcessos = atualizarStatusClientePorProcessos;
    }

    @Transactional
    public void deletarProcesso(Long id) {
        ProcessoApp processo = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id));

        Cliente cliente = processo.getCliente();

        // 1. Remove os vínculos com documentos para não violar foreign key constraint
        deleteProcessoDocumento.deletarVinculosPorProcessoAppId(id);

        // 2. Remove o histórico de alterações vinculado ao processo
        deleteHistoricoProcesso.deletarPorProcessoApp(processo);

        // 3. Remove o processo (ProcessoApi vinculado é removido via cascade JPA)
        processoAppRepository.delete(processo);

        // 4. Atualiza o status do cliente caso ele não possua outros casos ativos
        if (cliente != null) {
            atualizarStatusClientePorProcessos.atualizarStatusAposAlteracaoOuExclusao(cliente, id);
        }
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
