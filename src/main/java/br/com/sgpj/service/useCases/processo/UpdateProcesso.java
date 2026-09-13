package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.entity.Processo.*;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.MotivoStatusProcessoRepository;
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
    private final MotivoStatusProcessoRepository motivoStatusProcessoRepository;

    public UpdateProcesso(ProcessoAppRepository processoAppRepository,
                          PrioridadeRepository prioridadeRepository,
                          FaseProcessoRepository faseProcessoRepository,
                          ProcessoApiGateway processoApiGateway,
                          MotivoStatusProcessoRepository motivoStatusProcessoRepository) {
        this.processoAppRepository = processoAppRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.processoApiGateway = processoApiGateway;
        this.motivoStatusProcessoRepository = motivoStatusProcessoRepository;
    }

    public ProcessoAppUpdateResponse atualizarProcesso(Long id, ProcessoAppUpdateRequest processoAtualizar) {
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

        StatusProcesso novoStatus = processoEditado.getStatus();

        // Suspensão ou encerramento: motivo obrigatório, preencher campos de auditoria
        if (novoStatus == StatusProcesso.SUSPENSO || novoStatus == StatusProcesso.ENCERRADO) {
            if (processoAtualizar.motivoStatusProcessoId() == null) {
                throw new OperacaoInvalidaException(
                        "O motivo é obrigatório ao suspender ou encerrar um processo.");
            }

            MotivoStatusProcesso motivo = motivoStatusProcessoRepository
                    .findById(processoAtualizar.motivoStatusProcessoId())
                    .orElseThrow(() -> new ObjetoNaoEncontradoException(
                            "Motivo de status não encontrado para o ID: " + processoAtualizar.motivoStatusProcessoId()));

            boolean valido = motivo.getAplicavelA() == MotivoStatusProcesso.AplicavelA.AMBOS
                    || (novoStatus == StatusProcesso.SUSPENSO
                    && motivo.getAplicavelA() == MotivoStatusProcesso.AplicavelA.SUSPENSO)
                    || (novoStatus == StatusProcesso.ENCERRADO
                    && motivo.getAplicavelA() == MotivoStatusProcesso.AplicavelA.ENCERRADO);

            if (!valido) {
                throw new OperacaoInvalidaException(
                        "O motivo selecionado não é aplicável ao status do processo.");
            }
            processoEditado.setMotivoStatus(motivo);
            processoEditado.setEncerradoSuspensoEm(LocalDateTime.now());
            processoEditado.setEncerradoSuspensoPorUsuario(processoEncontrado.getUsuarioResponsavel());
        }

        // Reativação: limpar campos de suspensão/encerramento
        if (novoStatus == StatusProcesso.ATIVO) {
            processoEditado.setEncerradoSuspensoEm(null);
            processoEditado.setEncerradoSuspensoPorUsuario(null);
            processoEditado.setMotivoStatus(null);
            processoEditado.setJustificativa(null);
        }

        if (processoEncontrado.getProcessoApi() == null) {
            if (processoAtualizar.numeroCnj() != null) {
                ProcessoApi processoApi = processoApiGateway.buscarDadosOficiais(
                        processoAtualizar.numeroCnj(), processoEncontrado.getSiglaTribunal());
                processoEditado.setProcessoApi(processoApi);
            }
        } else {
            processoEditado.setProcessoApi(processoEncontrado.getProcessoApi());
        }

        return ProcessoAppMapper.toUpdateResponse(processoAppRepository.save(processoEditado));
    }
}
