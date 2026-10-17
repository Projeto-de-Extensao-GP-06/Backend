package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.*;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.repository.*;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateProcesso {

    private static final Logger log = LoggerFactory.getLogger(CreateProcesso.class);

    private final PrioridadeRepository prioridadeRepository;
    private final FaseProcessoRepository faseProcessoRepository;
    private final SiglaTribunalRepository siglaTribunalRepository;
    private final ClienteRepository clienteRepository;
    private final ProcessoApiGateway processoApiGateway;
    private final ProcessoAppRepository processoAppRepository;
    private final RegistroHistoricoProcesso registroHistorico;

    public CreateProcesso(PrioridadeRepository prioridadeRepository,
                          FaseProcessoRepository faseProcessoRepository,
                          SiglaTribunalRepository siglaTribunalRepository,
                          ClienteRepository clienteRepository,
                          ProcessoApiGateway processoApiGateway,
                          ProcessoAppRepository processoAppRepository,
                          RegistroHistoricoProcesso registroHistorico) {
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.siglaTribunalRepository = siglaTribunalRepository;
        this.clienteRepository = clienteRepository;
        this.processoApiGateway = processoApiGateway;
        this.processoAppRepository = processoAppRepository;
        this.registroHistorico = registroHistorico;
    }

    @Transactional
    public ProcessoAppCreateResponse criarProcesso(ProcessoAppCriacaoRequest novoProcesso, Usuario usuarioResponsavel) {
        Prioridade prioridade = prioridadeRepository.findById(novoProcesso.prioridadeId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Prioridade nao encontrada para o ID: " + novoProcesso.prioridadeId()));
        FaseProcesso faseProcesso = faseProcessoRepository.findById(novoProcesso.faseProcessoId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fase do processo nao encontrada para o ID: " + novoProcesso.faseProcessoId()));
        SiglaTribunal siglaTribunal = siglaTribunalRepository.findById(novoProcesso.siglaTribunalId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Sigla do tribunal nao encontrada para o ID: " + novoProcesso.siglaTribunalId()));
        Cliente cliente = clienteRepository.findById(novoProcesso.clienteId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Cliente nao encontrada para o ID: " + novoProcesso.clienteId()));

        ProcessoApp processoApp = ProcessoAppMapper.toCreateEntity(novoProcesso);

        processoApp.setUsuarioResponsavel(usuarioResponsavel);
        processoApp.setCliente(cliente);
        processoApp.setPrioridade(prioridade);
        processoApp.setFaseProcesso(faseProcesso);
        processoApp.setSiglaTribunal(siglaTribunal);

        String cnjNormalizado = null;
        if (novoProcesso.numeroProcesso() != null && !novoProcesso.numeroProcesso().isBlank()) {
            cnjNormalizado = novoProcesso.numeroProcesso().replaceAll("\\D", "");
            if (cnjNormalizado.isBlank()) {
                cnjNormalizado = null;
            }
        }

        boolean sincronizado = false;
        if (cnjNormalizado != null) {
            ProcessoApi dadosOficiais = null;
            try {
                dadosOficiais = processoApiGateway.buscarDadosOficiais(cnjNormalizado, siglaTribunal);
            } catch (Exception e) {
                log.warn("Falha ao buscar dados no DataJud para o CNJ {}: {}", cnjNormalizado, e.getMessage());
            }

            if (dadosOficiais != null) {
                dadosOficiais.setNumeroCnj(cnjNormalizado);
                processoApp.setProcessoApi(dadosOficiais);
                sincronizado = true;
            } else {
                ProcessoApi processoApi = new ProcessoApi();
                processoApi.setNumeroCnj(cnjNormalizado);
                if (siglaTribunal != null) {
                    processoApi.setTribunalTramitacao(siglaTribunal.getNome());
                }
                processoApp.setProcessoApi(processoApi);
                sincronizado = false;
            }
        }

        ProcessoApp salvo = processoAppRepository.save(processoApp);
        registroHistorico.registrar(salvo, usuarioResponsavel, RegistroHistoricoProcesso.ABERTURA, "Processo criado.");

        return ProcessoAppMapper.toCreateResponse(salvo, sincronizado, cnjNormalizado);
    }
}
