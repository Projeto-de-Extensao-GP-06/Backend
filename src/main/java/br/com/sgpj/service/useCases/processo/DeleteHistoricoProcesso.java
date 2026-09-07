package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import org.springframework.stereotype.Component;

@Component
public class DeleteHistoricoProcesso {

    private final HistoricoProcessoRepository historicoProcessoRepository;

    public DeleteHistoricoProcesso(HistoricoProcessoRepository historicoProcessoRepository) {
        this.historicoProcessoRepository = historicoProcessoRepository;
    }

    public void deletarPorProcessoApp(ProcessoApp processoApp) {
        historicoProcessoRepository.deleteByProcessoApp(processoApp);
    }
}
