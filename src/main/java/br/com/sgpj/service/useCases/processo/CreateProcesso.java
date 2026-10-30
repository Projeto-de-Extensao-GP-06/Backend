package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.*;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.mapper.SiglaTribunalMapper;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import br.com.sgpj.repository.*;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateProcesso {

    private final PrioridadeRepository prioridadeRepository;
    private final FaseProcessoRepository faseProcessoRepository;
    private final SiglaTribunalRepository siglaTribunalRepository;
    private final ClienteRepository clienteRepository;
    private final SincronizacaoProcessoProducer sincronizacaoProducer;
    private final ProcessoAppRepository processoAppRepository;
    private final RegistroHistoricoProcesso registroHistorico;

    public CreateProcesso(PrioridadeRepository prioridadeRepository,
                          FaseProcessoRepository faseProcessoRepository,
                          SiglaTribunalRepository siglaTribunalRepository,
                          ClienteRepository clienteRepository,
                          SincronizacaoProcessoProducer sincronizacaoProducer,
                          ProcessoAppRepository processoAppRepository,
                          RegistroHistoricoProcesso registroHistorico) {
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.siglaTribunalRepository = siglaTribunalRepository;
        this.clienteRepository = clienteRepository;
        this.sincronizacaoProducer = sincronizacaoProducer;
        this.processoAppRepository = processoAppRepository;
        this.registroHistorico = registroHistorico;
    }

    @Transactional
    public ProcessoAppCreateResponse criarProcesso(ProcessoAppCriacaoRequest novoProcesso, Usuario usuarioResponsavel) {
        Prioridade prioridade = prioridadeRepository.findById(novoProcesso.prioridadeId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Prioridade nao encontrada para o ID: " + novoProcesso.prioridadeId()));
        FaseProcesso faseProcesso = faseProcessoRepository.findById(novoProcesso.faseProcessoId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fase do processo nao encontrada para o ID: " + novoProcesso.faseProcessoId()));
        Cliente cliente = clienteRepository.findById(novoProcesso.clienteId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Cliente nao encontrada para o ID: " + novoProcesso.clienteId()));

        ProcessoApp processoApp = ProcessoAppMapper.toCreateEntity(novoProcesso);

        processoApp.setUsuarioResponsavel(usuarioResponsavel);
        processoApp.setCliente(cliente);
        processoApp.setPrioridade(prioridade);
        processoApp.setFaseProcesso(faseProcesso);

        String cnjNormalizado = null;
        if (novoProcesso.numeroProcesso() != null && !novoProcesso.numeroProcesso().isBlank()) {
            cnjNormalizado = novoProcesso.numeroProcesso().replaceAll("\\D", "");
            if (cnjNormalizado.isBlank()) {
                cnjNormalizado = null;
            }
        }

        if (cnjNormalizado != null && processoAppRepository.existsByCnj(cnjNormalizado)) {
            throw new br.com.sgpj.exception.ProcessoJaExistenteException("Já existe um processo cadastrado com este número de CNJ.");
        }

        if (cnjNormalizado != null) {
            String sigla = SiglaTribunalMapper.mapearSiglaTribunalPorNumero(cnjNormalizado.substring(14, 16));
            SiglaTribunal siglaTribunal = siglaTribunalRepository.findByNomeIgnoreCase(sigla)
                    .orElseThrow(() -> new ObjetoNaoEncontradoException("Sigla do tribunal nao encontrada: " + sigla));

            processoApp.setSiglaTribunal(siglaTribunal);

            // Dados oficiais são buscados de forma assíncrona pela fila de sincronização.
            ProcessoApi processoApi = new ProcessoApi();
            processoApi.setNumeroCnj(cnjNormalizado);
            processoApi.setTribunalTramitacao(siglaTribunal.getNome());
            processoApp.setProcessoApi(processoApi);
        }

        ProcessoApp salvo = processoAppRepository.save(processoApp);
        registroHistorico.registrar(salvo, usuarioResponsavel, RegistroHistoricoProcesso.ABERTURA, "Processo criado.");
        if (cnjNormalizado != null) {
            sincronizacaoProducer.enviarAposCommit(salvo.getId());
        }

        return ProcessoAppMapper.toCreateResponse(salvo, false, cnjNormalizado);
    }
}
