package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.*;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.repository.*;
import org.springframework.stereotype.Component;

@Component
public class CreateProcesso {

    private final PrioridadeRepository prioridadeRepository;
    private final FaseProcessoRepository faseProcessoRepository;
    private final SiglaTribunalRepository siglaTribunalRepository;
    private final ClienteRepository clienteRepository;
    private final ProcessoApiGateway processoApiGateway;
    private final ProcessoAppRepository processoAppRepository;

    public CreateProcesso(PrioridadeRepository prioridadeRepository, FaseProcessoRepository faseProcessoRepository, SiglaTribunalRepository siglaTribunalRepository, ClienteRepository clienteRepository, ProcessoApiGateway processoApiGateway, ProcessoAppRepository processoAppRepository) {
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.siglaTribunalRepository = siglaTribunalRepository;
        this.clienteRepository = clienteRepository;
        this.processoApiGateway = processoApiGateway;
        this.processoAppRepository = processoAppRepository;
    }

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

        if(novoProcesso.numeroProcesso() != null){
            ProcessoApi processoApi = processoApiGateway.buscarDadosOficiais(novoProcesso.numeroProcesso());
            processoApp.setProcessoApi(processoApi);
        }

        processoApp.setUsuarioResponsavel(usuarioResponsavel);
        processoApp.setCliente(cliente);
        processoApp.setPrioridade(prioridade);
        processoApp.setFaseProcesso(faseProcesso);
        processoApp.setSiglaTribunal(siglaTribunal);


        return ProcessoAppMapper.toCreateResponse(processoAppRepository.save(processoApp));
    }

}
