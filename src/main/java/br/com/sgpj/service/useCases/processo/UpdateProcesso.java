package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.*;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.mapper.HistoricoProcessoMapper;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.mapper.SiglaTribunalMapper;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import br.com.sgpj.repository.*;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import br.com.sgpj.service.useCases.cliente.AtualizarStatusClientePorProcessos;
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
    private final AuthorizationUtil authorizationUtil;
    private final SincronizacaoProcessoProducer sincronizacaoProducer;
    private final UsuarioRepository usuarioRepository;
    private final AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos;

    public UpdateProcesso(ProcessoAppRepository processoAppRepository,
                          PrioridadeRepository prioridadeRepository,
                          FaseProcessoRepository faseProcessoRepository,
                          SiglaTribunalRepository siglaTribunalRepository,
                          ProcessoApiGateway processoApiGateway,
                          MotivoStatusProcessoRepository motivoStatusProcessoRepository,
                          MovimentacoesOficiaisRepository movimentacoesOficiaisRepository,
                          ProcessoApiRepository processoApiRepository,
                          HistoricoProcessoRepository historicoProcessoRepository,
                          RegistroHistoricoProcesso registroHistorico,
                          AuthorizationUtil authorizationUtil,
                          SincronizacaoProcessoProducer sincronizacaoProducer,
                          UsuarioRepository usuarioRepository,
                          AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos) {
        this.sincronizacaoProducer = sincronizacaoProducer;
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
        this.authorizationUtil = authorizationUtil;
        this.usuarioRepository = usuarioRepository;
        this.atualizarStatusClientePorProcessos = atualizarStatusClientePorProcessos;
    }

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
            processoEditado.setEncerradoSuspensoPorUsuario(authorizationUtil.getUsuarioLogado());
        }

        // Reativação: limpar campos de suspensão/encerramento
        if (novoStatus == StatusProcesso.ATIVO) {
            processoEditado.setEncerradoSuspensoEm(null);
            processoEditado.setEncerradoSuspensoPorUsuario(null);
            processoEditado.setMotivoStatus(null);
            processoEditado.setJustificativa(null);
        }

        boolean sincronizarDataJud = false;
        if (processoEncontrado.getProcessoApi() == null) {
            if (processoAtualizar.numeroCnj() != null && !processoAtualizar.numeroCnj().isBlank()) {
                String cnjLimpo = processoAtualizar.numeroCnj().replaceAll("\\D", "");
                if (!cnjLimpo.isBlank()) {
                    if (processoAppRepository.existsByCnjAndIdNot(cnjLimpo, id)) {
                        throw new br.com.sgpj.exception.ProcessoJaExistenteException("Já existe outro processo cadastrado com este número de CNJ.");
                    }

                    String sigla = SiglaTribunalMapper.mapearSiglaTribunalPorNumero(cnjLimpo.substring(14, 16));
                    SiglaTribunal siglaTribunal = siglaTribunalRepository.findByNomeIgnoreCase(sigla)
                            .orElseThrow(() -> new ObjetoNaoEncontradoException("Sigla do tribunal não encontrada: " + sigla));

                    processoEditado.setSiglaTribunal(siglaTribunal);

                    ProcessoApi processoApi = new ProcessoApi();
                    processoApi.setNumeroCnj(cnjLimpo);
                    processoApi.setTribunalTramitacao(siglaTribunal.getNome());
                    processoEditado.setProcessoApi(processoApi);
                    sincronizarDataJud = true;
                }
            }
        } else {
            processoEditado.setProcessoApi(processoEncontrado.getProcessoApi());
        }

        ProcessoApp processoSalvo = processoAppRepository.save(processoEditado);

        Cliente cliente = processoEncontrado.getCliente();
        if (cliente != null) {
            if (novoStatus == StatusProcesso.ATIVO) {
                atualizarStatusClientePorProcessos.ativarCliente(cliente);
            } else {
                atualizarStatusClientePorProcessos.atualizarStatusAposAlteracaoOuExclusao(cliente, id);
            }
        }

        registrarAlteracao(statusAnterior, observacaoAnterior, processoSalvo, processoAtualizar.descricaoAlteracao());
        if (sincronizarDataJud) {
            sincronizacaoProducer.enviarAposCommit(processoSalvo.getId());
        }
        return ProcessoAppMapper.toUpdateResponse(processoSalvo);
    }

    private void registrarAlteracao(StatusProcesso statusAnterior, String observacaoAnterior, ProcessoApp atual, String descricao) {
        String descricaoNormalizada = normalizarDescricao(descricao);
        if (statusAnterior != atual.getStatus()) {
            registroHistorico.registrar(atual, authorizationUtil.getUsuarioLogado(), RegistroHistoricoProcesso.STATUS,
                    descricaoNormalizada != null ? descricaoNormalizada :"Status alterado de " + statusAnterior + " para " + atual.getStatus() + ".");
        } else if (!java.util.Objects.equals(observacaoAnterior, atual.getObservacao())) {
            registroHistorico.registrar(atual, authorizationUtil.getUsuarioLogado(), RegistroHistoricoProcesso.OBSERVACAO,
                    descricaoNormalizada != null ? descricaoNormalizada :"Observações atualizadas.");
        } else {
            registroHistorico.registrar(atual, authorizationUtil.getUsuarioLogado(), RegistroHistoricoProcesso.ATUALIZACAO,
                    descricaoNormalizada != null ? descricaoNormalizada : "Dados do processo atualizados.");
        }
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim();
    }

    @Transactional
    public ProcessoAppResponse atualizarDadosDoDataJud(Long id) {
        ProcessoApp processoApp = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id));

        ProcessoApi processoApiExistente = processoApp.getProcessoApi();
        if (processoApiExistente == null || processoApiExistente.getNumeroCnj() == null || processoApiExistente.getNumeroCnj().isBlank()) {
            throw new OperacaoInvalidaException("Este processo não possui número CNJ vinculado para atualização.");
        }

        if (processoApp.getSiglaTribunal() == null) {
            throw new OperacaoInvalidaException("Este processo não possui tribunal vinculado para atualização.");
        }

        ProcessoApi dadosAtualizados = processoApiGateway.buscarDadosOficiaisOuFalhar(
                processoApiExistente.getNumeroCnj(), processoApp.getSiglaTribunal().getNome());

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
        registroHistorico.registrar(processoApp, usuarioRepository.findById(Usuario.ID_SISTEMA).get(),
                RegistroHistoricoProcesso.ATUALIZACAO,
                "Dados oficiais do processo atualizados.");

        List<HistoricoProcessoResponse> historico = historicoProcessoRepository
                .findByProcessoApp(processoApp).stream()
                .map(HistoricoProcessoMapper::toResponse)
                .toList();

        return ProcessoAppMapper.toResponse(processoApp, historico);
    }
}
