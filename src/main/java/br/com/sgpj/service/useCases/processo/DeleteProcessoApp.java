package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteProcessoApp {

    private final ProcessoAppRepository processoAppRepository;

    public DeleteProcessoApp(ProcessoAppRepository processoAppRepository) {
        this.processoAppRepository = processoAppRepository;
    }

    public void deletarProcesso(Long id) {
        if (!processoAppRepository.existsById(id)) {
            throw new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id);
        }
        processoAppRepository.deleteById(id);
    }

    public void deletarProcessos(List<ProcessoApp> processos) {
        if (processos != null && !processos.isEmpty()) {
            processoAppRepository.deleteAll(processos);
        }
    }
}
