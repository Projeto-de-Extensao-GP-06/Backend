package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.Processos.SincronizacaoProcessoResponse;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SolicitarSincronizacaoSolicitacao {

    private final ProcessoAppRepository processoAppRepository;
    private final PrepararVinculoProcessoOficial prepararVinculoProcessoOficial;
    private final SincronizacaoProcessoProducer sincronizacaoProducer;

    public SolicitarSincronizacaoSolicitacao(ProcessoAppRepository processoAppRepository,
                                             PrepararVinculoProcessoOficial prepararVinculoProcessoOficial,
                                             SincronizacaoProcessoProducer sincronizacaoProducer) {
        this.processoAppRepository = processoAppRepository;
        this.prepararVinculoProcessoOficial = prepararVinculoProcessoOficial;
        this.sincronizacaoProducer = sincronizacaoProducer;
    }

    @Transactional
    public SincronizacaoProcessoResponse solicitar(Long id) {
        ProcessoApp processoApp = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Solicitação não encontrada para o ID: " + id));

        ProcessoApi processoApi = processoApp.getProcessoApi();
        String cnj = processoApi != null ? processoApi.getNumeroCnj() : null;

        if (cnj == null || cnj.isBlank()) {
            throw new OperacaoInvalidaException("Esta solicitação não possui número CNJ vinculado para atualização.");
        }

        if (processoApp.getSiglaTribunal() == null) {
            boolean preparado = prepararVinculoProcessoOficial.preparar(processoApp, cnj);
            if (!preparado || processoApp.getSiglaTribunal() == null) {
                throw new OperacaoInvalidaException("Não foi possível identificar o tribunal a partir do CNJ desta solicitação.");
            }
            processoAppRepository.save(processoApp);
        }

        sincronizacaoProducer.enviarSolicitacao(id);

        return new SincronizacaoProcessoResponse(
                id,
                "Solicitação de sincronização enviada para a fila de processamento.",
                "EM_PROCESSAMENTO"
        );
    }
}
