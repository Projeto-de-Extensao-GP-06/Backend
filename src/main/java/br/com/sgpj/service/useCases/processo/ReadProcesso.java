package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppMiniResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.HistoricoProcessoMapper;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.security.AuthorizationUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReadProcesso {

    private static final String OBSERVACAO_SOLICITACAO_PREFIXO = "Solicitação enviada pelo formulário público. Estado: ";

    private final ProcessoAppRepository processoAppRepository;
    private final AuthorizationUtil authorizationUtil;
    private final HistoricoProcessoRepository historicoProcessoRepository;
    private final ClienteRepository clienteRepository;

    public ReadProcesso(ProcessoAppRepository processoAppRepository, AuthorizationUtil authorizationUtil, HistoricoProcessoRepository historicoProcessoRepository, ClienteRepository clienteRepository) {
        this.processoAppRepository = processoAppRepository;
        this.authorizationUtil = authorizationUtil;
        this.historicoProcessoRepository = historicoProcessoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<ProcessoAppMiniResponse> listarProcessos(){

        List<ProcessoApp> processosEncontrados = processoAppRepository.findAll();
        List<ProcessoAppMiniResponse> processos = new ArrayList<>();

        for (ProcessoApp processo : processosEncontrados){
            if (deveExibirComoProcesso(processo)) {
                processos.add(ProcessoAppMapper.toMiniResponse(processo));
            }
        }
        return processos;
    }

    public ProcessoAppResponse buscarPorId(Long id) {
        authorizationUtil.verificarAcessoProcesso(id);
        ProcessoApp processoAppEncontrado = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id));
        if (!deveExibirComoProcesso(processoAppEncontrado)) {
            throw new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id);
        }
        List<HistoricoProcesso> historicoProcessoList = historicoProcessoRepository.findByProcessoApp(processoAppEncontrado);

        List<HistoricoProcessoResponse> historicoEncontrado = new ArrayList<>();

        for (HistoricoProcesso historicoProcesso : historicoProcessoList) {
            historicoEncontrado.add(HistoricoProcessoMapper.toResponse(historicoProcesso));
        }

        return ProcessoAppMapper.toResponse(processoAppEncontrado, historicoEncontrado);
    }

    public List<ProcessoAppMiniResponse> buscarPorClienteId(Long clienteId){
        authorizationUtil.verificarAcessoCliente(clienteId);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Cliente não encontrado para o ID: " + clienteId));

        List<ProcessoApp> processosEncontrados = processoAppRepository.findByCliente(cliente);
        List<ProcessoAppMiniResponse> processos = new ArrayList<>();

        for (ProcessoApp processo : processosEncontrados){
            if (deveExibirComoProcesso(processo)) {
                processos.add(ProcessoAppMapper.toMiniResponse(processo));
            }
        }
        return processos;
    }

    public List<ProcessoAppMiniResponse> buscaFiltro(String query, Long faseId, Long tribunalId){
        String queryNormalizada = (query == null || query.isBlank()) ? null : query.trim();
        List<ProcessoApp> processosEncontrados = processoAppRepository.buscaFiltro(queryNormalizada, faseId, tribunalId);


        List<ProcessoAppMiniResponse> processos = new ArrayList<>();

        for (ProcessoApp processo : processosEncontrados){
            if (deveExibirComoProcesso(processo)) {
                processos.add(ProcessoAppMapper.toMiniResponse(processo));
            }
        }
        return processos;
    }



    private boolean deveExibirComoProcesso(ProcessoApp processo) {
        return !isSolicitacaoAindaNaoAceita(processo);
    }

    private boolean isSolicitacaoAindaNaoAceita(ProcessoApp processo) {
        return processo.getObservacao() != null
                && processo.getObservacao().startsWith(OBSERVACAO_SOLICITACAO_PREFIXO)
                && !Boolean.TRUE.equals(processo.getAtivo());
    }
}
