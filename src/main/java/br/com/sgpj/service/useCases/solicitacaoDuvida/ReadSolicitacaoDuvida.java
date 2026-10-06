package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.SolicitacaoDuvida;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.ProcessoAppToSolicitacaoDuvidaMapper;
import br.com.sgpj.mapper.SolicitacaoDuvidaMapper;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.stereotype.Component;

@Component
public class ReadSolicitacaoDuvida {

    private final ProcessoAppRepository processoAppRepository;

    public ReadSolicitacaoDuvida(ProcessoAppRepository processoAppRepository) {
        this.processoAppRepository = processoAppRepository;
    }

    public SolicitacaoDuvidaDetalhesResponse buscarPorId(Long id) {
        return SolicitacaoDuvidaMapper.toDetalhesResponse(buscarSolicitacao(id));
    }

    private SolicitacaoDuvida buscarSolicitacao(Long id) {
        ProcessoApp processoApp = buscarProcessoSolicitacao(id);
        return ProcessoAppToSolicitacaoDuvidaMapper.toSolicitacaoDuvida(processoApp);
    }

    private ProcessoApp buscarProcessoSolicitacao(Long id) {
        return processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Solicitação não encontrada para o ID: " + id));
    }
}
