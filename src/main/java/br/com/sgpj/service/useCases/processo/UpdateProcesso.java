package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.entity.Processo.*;
import br.com.sgpj.exception.DataJudIntegrationException;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.mapper.HistoricoProcessoMapper;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.repository.*;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UpdateProcesso {

    private final ProcessoAppRepository processoAppRepository;
    private final PrioridadeRepository prioridadeRepository;
    private final FaseProcessoRepository faseProcessoRepository;
    private final SiglaTribunalRepository siglaTribunalRepository;
    private final ProcessoApiGateway processoApiGateway;
    private final MotivoStatusProcessoRepository motivoStatusProcessoRepository;
    private final MovimentacoesOficiaisRepository movimentacoesOficiaisRepository;
    private final ProcessoApiRepository processoApiRepository;
    private final HistoricoProcessoRepository historicoProcessoRepository;
    private final RegistroHistoricoProcesso registroHistorico;

    public UpdateProcesso(ProcessoAppRepository processoAppRepository,
                           PrioridadeRepository prioridadeRepository,
                           FaseProcessoRepository faseProcessoRepository,
                           SiglaTribunalRepository siglaTribunalRepository,
                           ProcessoApiGateway processoApiGateway,
                          MotivoStatusProcessoRepository motivoStatusProcessoRepository,
                           MovimentacoesOficiaisRepository movimentacoesOficiaisRepository,
                           ProcessoApiRepository processoApiRepository,
                           HistoricoProcessoRepository historicoProcessoRepository,
                           RegistroHistoricoProcesso registroHistorico) {
        this.processoAppRepository = processoAppRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.siglaTribunalRepository = siglaTribunalRepository;
        this.processoApiGateway = processoApiGateway;
        this.motivoStatusProcessoRepository = motivoStatusProcessoRepository;
        this.movimentacoesOficiaisRepository = movimentacoesOficiaisRepository;
        this.processoApiRepository = processoApiRepository;
        this.historicoProcessoRepository = historicoProcessoRepository;
        this.registroHistorico = registroHistorico;
    }

    // ... resto da classe continua igual

    @Transactional
    public ProcessoAppUpdateResponse atualizarProcesso(Long id, ProcessoAppUpdateRequest processoAtualizar) {
        ProcessoApp processoEncontrado = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id));
        StatusProcesso statusAnterior = processoEncontrado.getStatus();
        String observacaoAnterior = processoEncontrado.getObservacao();

        Prioridade prioridade = prioridadeRepository.findById(processoAtualizar.prioridadeId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Prioridade não encontrada para o ID: " + processoAtualizar.prioridadeId()));
        FaseProcesso faseProcesso = faseProcessoRepository.findById(processoAtualizar.faseProcessoId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fase do processo nao encontrada para o ID: " + processoAtualizar.faseProcessoId()));
        SiglaTribunal siglaTribunal = processoAtualizar.siglaTribunalId() == null
                ? processoEncontrado.getSiglaTribunal()
                : siglaTribunalRepository.findById(processoAtualizar.siglaTribunalId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Sigla do tribunal não encontrada para o ID: " + processoAtualizar.siglaTribunalId()));

        ProcessoApp processoEditado = ProcessoAppMapper.toUpdateEntity(processoAtualizar);

        processoEditado.setId(processoEncontrado.getId());
        processoEditado.setUsuarioResponsavel(processoEncontrado.getUsuarioResponsavel());
        processoEditado.setCliente(processoEncontrado.getCliente());
        processoEditado.setSiglaTribunal(siglaTribunal);
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

        if (processoAtualizar.numeroCnj() != null && !processoAtualizar.numeroCnj().isBlank()) {
            String cnjLimpo = processoAtualizar.numeroCnj().replaceAll("\\D", "");
            if (!cnjLimpo.isBlank() && processoAppRepository.existsByCnjAndIdNot(cnjLimpo, id)) {
                throw new br.com.sgpj.exception.ProcessoJaExistenteException("Já existe outro processo cadastrado com este número de CNJ.");
            }
        }

        if (processoEncontrado.getProcessoApi() == null) {
            if (processoAtualizar.numeroCnj() != null && !processoAtualizar.numeroCnj().isBlank()) {
                ProcessoApi processoApi = null;
                try {
                    processoApi = processoApiGateway.buscarDadosOficiais(
                            processoAtualizar.numeroCnj(), siglaTribunal);
                } catch (Exception e) {
                    // Ignora erro da API externa
                }
                if (processoApi == null) {
                    processoApi = new ProcessoApi();
                    processoApi.setNumeroCnj(processoAtualizar.numeroCnj());
                    if (siglaTribunal != null) {
                        processoApi.setTribunalTramitacao(siglaTribunal.getNome());
                    }
                }
                processoEditado.setProcessoApi(processoApi);
            }
        } else {
            ProcessoApi processoApi = processoEncontrado.getProcessoApi();
            if (processoAtualizar.numeroCnj() != null && !processoAtualizar.numeroCnj().isBlank()) {
                processoApi.setNumeroCnj(processoAtualizar.numeroCnj());
            }
            processoEditado.setProcessoApi(processoApi);
        }

        ProcessoApp processoSalvo = processoAppRepository.save(processoEditado);
        registrarAlteracao(statusAnterior, observacaoAnterior, processoSalvo);
        return ProcessoAppMapper.toUpdateResponse(processoSalvo);
    }

    private void registrarAlteracao(StatusProcesso statusAnterior, String observacaoAnterior, ProcessoApp atual) {
        if (statusAnterior != atual.getStatus()) {
            registroHistorico.registrar(atual, atual.getUsuarioResponsavel(), RegistroHistoricoProcesso.STATUS,
                    "Status alterado de " + statusAnterior + " para " + atual.getStatus() + ".");
        } else if (!java.util.Objects.equals(observacaoAnterior, atual.getObservacao())) {
            registroHistorico.registrar(atual, atual.getUsuarioResponsavel(), RegistroHistoricoProcesso.OBSERVACAO,
                    "Observações atualizadas.");
        } else {
            registroHistorico.registrar(atual, atual.getUsuarioResponsavel(), RegistroHistoricoProcesso.ATUALIZACAO,
                    "Dados do processo atualizados.");
        }
    }

    @Transactional
    public ProcessoAppResponse atualizarDadosDoDataJud(Long id) {
        ProcessoApp processoApp = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id));

        ProcessoApi processoApiExistente = processoApp.getProcessoApi();
        if (processoApiExistente == null || processoApiExistente.getNumeroCnj() == null) {
            throw new OperacaoInvalidaException("Este processo não possui número CNJ vinculado para atualização.");
        }

        ProcessoApi dadosAtualizados = processoApiGateway.buscarDadosOficiais(
                processoApiExistente.getNumeroCnj(), processoApp.getSiglaTribunal());

        if (dadosAtualizados == null) {
            throw new DataJudIntegrationException("Não foi possível obter dados oficiais do DataJud no momento. Por favor, tente novamente mais tarde.");
        }

        processoApiExistente.setTribunalTramitacao(dadosAtualizados.getTribunalTramitacao());
        processoApiExistente.setInstancia(dadosAtualizados.getInstancia());
        processoApiExistente.setDataAjuizamento(dadosAtualizados.getDataAjuizamento());
        processoApiExistente.setNivelSigilo(dadosAtualizados.getNivelSigilo());
        processoApiExistente.setClasseProcessual(dadosAtualizados.getClasseProcessual());
        processoApiExistente.setFormato(dadosAtualizados.getFormato());
        processoApiExistente.setSistemaTribunal(dadosAtualizados.getSistemaTribunal());
        processoApiExistente.setOrgaoJulgador(dadosAtualizados.getOrgaoJulgador());
        processoApiExistente.setAssunto(dadosAtualizados.getAssunto());

        if (processoApiExistente.getMovimentacoes() != null && !processoApiExistente.getMovimentacoes().isEmpty()) {
            movimentacoesOficiaisRepository.deleteAll(processoApiExistente.getMovimentacoes());
        }

        List<MovimentacoesOficiais> novasMovimentacoes = dadosAtualizados.getMovimentacoes();
        if (novasMovimentacoes != null) {
            for (MovimentacoesOficiais mov : novasMovimentacoes) {
                mov.setId(null);
                mov.setProcessoApi(processoApiExistente);
            }
        }
        processoApiExistente.setMovimentacoes(novasMovimentacoes);

        processoApiRepository.save(processoApiExistente);

        processoApp.setUltimaAtualizacao(LocalDateTime.now());
        processoAppRepository.save(processoApp);
        registroHistorico.registrar(processoApp, processoApp.getUsuarioResponsavel(), RegistroHistoricoProcesso.ATUALIZACAO,
                "Dados oficiais do processo atualizados.");

        List<HistoricoProcessoResponse> historico = historicoProcessoRepository
                .findByProcessoApp(processoApp).stream()
                .map(HistoricoProcessoMapper::toResponse)
                .toList();

        return ProcessoAppMapper.toResponse(processoApp, historico);
    }
}
