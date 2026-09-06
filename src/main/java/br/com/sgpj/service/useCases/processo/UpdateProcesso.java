package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.PrioridadeRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UpdateProcesso {

    private final ProcessoAppRepository processoAppRepository;
    private final PrioridadeRepository prioridadeRepository;
    private final FaseProcessoRepository faseProcessoRepository;
    private final ProcessoApiGateway processoApiGateway;

    public UpdateProcesso(ProcessoAppRepository processoAppRepository, PrioridadeRepository prioridadeRepository, FaseProcessoRepository faseProcessoRepository, ProcessoApiGateway processoApiGateway) {
        this.processoAppRepository = processoAppRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.processoApiGateway = processoApiGateway;
    }

    public ProcessoAppUpdateResponse atualizarProcesso(Long id, ProcessoAppUpdateRequest processoAtualizar){
        ProcessoApp processoEncontrado = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id));

        Prioridade prioridade = prioridadeRepository.findById(processoAtualizar.prioridadeId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Prioridade não encontrada para o ID: " + processoAtualizar.prioridadeId()));
        FaseProcesso faseProcesso = faseProcessoRepository.findById(processoAtualizar.faseProcessoId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fase do processo nao encontrada para o ID: " + processoAtualizar.faseProcessoId()));


        ProcessoApp processoEditado = ProcessoAppMapper.toUpdateEntity(processoAtualizar);

        processoEditado.setId(processoEncontrado.getId());
        processoEditado.setUsuarioResponsavel(processoEncontrado.getUsuarioResponsavel());
        processoEditado.setCliente(processoEncontrado.getCliente());
        processoEditado.setSiglaTribunal(processoEncontrado.getSiglaTribunal());
        processoEditado.setDataHoraInicio(processoEncontrado.getDataHoraInicio());
        processoEditado.setPrioridade(prioridade);
        processoEditado.setFaseProcesso(faseProcesso);

        if(processoEditado.getAtivo().equals(false)){
            processoEditado.setEncerradoEm(LocalDateTime.now());
            processoEditado.setEncerradoPorUsuario(processoEncontrado.getUsuarioResponsavel());
        }

        if(processoEncontrado.getProcessoApi() == null){
            if(processoAtualizar.numeroCnj() != null){
                ProcessoApi processoApi = processoApiGateway.buscarDadosOficiais(processoAtualizar.numeroCnj(), processoEncontrado.getSiglaTribunal());
                processoEditado.setProcessoApi(processoApi);
            }

        } else {
            processoEditado.setProcessoApi(processoEncontrado.getProcessoApi());

        }


        return ProcessoAppMapper.toUpdateResponse(processoAppRepository.save(processoEditado));
    }

}
