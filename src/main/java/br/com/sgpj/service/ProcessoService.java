package br.com.sgpj.service;

import br.com.sgpj.adapter.api.DataJudAdapter;
import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.*;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.*;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.exception.AcessoNegadoException;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.mapper.HistoricoProcessoMapper;
import br.com.sgpj.mapper.ProcessoApiMapper;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.repository.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class ProcessoService {
    private static final String OBSERVACAO_SOLICITACAO_PREFIXO = "Solicitação enviada pelo formulário público. Estado: ";

    private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
    private final DataJudAdapter dataJudAdapter;

    private final ProcessoAppRepository processoAppRepository;
    private final PrioridadeRepository prioridadeRepository;
    private final FaseProcessoRepository faseProcessoRepository;
    private final SiglaTribunalRepository siglaTribunalRepository;
    private final ClienteRepository clienteRepository;
    private final HistoricoProcessoRepository historicoProcessoRepository;
    private final DocumentoRepository documentoRepository;
    private final ProcessoDocumentoRepository processoDocumentoRepository;
    private final AuthorizationUtil authorizationUtil;

    public ProcessoService(UsuarioPersistenciaAdapter usuarioPersistenciaAdapter, DataJudAdapter dataJudAdapter, ProcessoAppRepository processoAppRepository, PrioridadeRepository prioridadeRepository, FaseProcessoRepository faseProcessoRepository, SiglaTribunalRepository siglaTribunalRepository, ClienteRepository clienteRepository, HistoricoProcessoRepository historicoProcessoRepository, DocumentoRepository documentoRepository, ProcessoDocumentoRepository processoDocumentoRepository, AuthorizationUtil authorizationUtil) {
        this.usuarioPersistenciaAdapter = usuarioPersistenciaAdapter;
        this.dataJudAdapter = dataJudAdapter;
        this.processoAppRepository = processoAppRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.siglaTribunalRepository = siglaTribunalRepository;
        this.clienteRepository = clienteRepository;
        this.historicoProcessoRepository = historicoProcessoRepository;
        this.documentoRepository = documentoRepository;
        this.processoDocumentoRepository = processoDocumentoRepository;
        this.authorizationUtil = authorizationUtil;
    }

    // ----CRIAÇÃO, ATUALIZAÇÃO E DELEÇÃO----
    public ProcessoApi buscarDadosOficiais(String numeroProcesso, SiglaTribunal siglaTribunal) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            return null;
        }

        String dataJudResponse = dataJudAdapter.buscarPorNumeroDeProcesso(numeroProcesso, siglaTribunal.getNome());
        JSONObject jsonDataJud = new JSONObject(dataJudResponse);

        return ProcessoApiMapper.toEntity(jsonDataJud);
    }

    public ProcessoAppCreateResponse criarProcesso(ProcessoAppCriacaoRequest novoProcesso, Usuario usuarioAutenticado) {
        Usuario usuarioResponsavel = usuarioAutenticado;
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
            ProcessoApi processoApi = buscarDadosOficiais(novoProcesso.numeroProcesso(), siglaTribunal);
            processoApp.setProcessoApi(processoApi);
        }

        processoApp.setUsuarioResponsavel(usuarioResponsavel);
        processoApp.setCliente(cliente);
        processoApp.setPrioridade(prioridade);
        processoApp.setFaseProcesso(faseProcesso);
        processoApp.setSiglaTribunal(siglaTribunal);


        return ProcessoAppMapper.toCreateResponse(processoAppRepository.save(processoApp));
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
                ProcessoApi processoApi = buscarDadosOficiais(processoAtualizar.numeroCnj(), processoEncontrado.getSiglaTribunal());
                processoEditado.setProcessoApi(processoApi);
            }

        } else {
            processoEditado.setProcessoApi(processoEncontrado.getProcessoApi());

        }


        return ProcessoAppMapper.toUpdateResponse(processoAppRepository.save(processoEditado));
    }


    public void deletarProcesso(Long id){

        if(!processoAppRepository.existsById(id)){
            throw new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id);
        }

        processoAppRepository.deleteById(id);
    }




    //--------

    // ----QUERY----
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
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo nÃ£o encontrado para o ID: " + id));
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
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Cliente nÃ£o encontrado para o ID: " + clienteId));

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


    // ------Metodos-------
    public ProcessoDocumentoResponse anexarDocumento(Long processoId, Long documentoId, Long usuarioId) {
        ProcessoApp processo = processoAppRepository.findById(processoId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado: " + processoId));

        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Documento não encontrado: " + documentoId));

        Usuario usuario = usuarioPersistenciaAdapter.buscarUsuarioPorId(usuarioId);

        ProcessoDocumentoId pdId = new ProcessoDocumentoId(documento.getId(), processo.getId());

        // Verifica se o vínculo já existe para evitar duplicata
        if (processoDocumentoRepository.existsById(pdId)) {
            throw new ObjetoNaoEncontradoException("Este documento já está vinculado a este processo.");
        }

        ProcessoDocumento processoDocumento = new ProcessoDocumento();
        processoDocumento.setId(pdId);
        processoDocumento.setDocumento(documento);
        processoDocumento.setProcessoApp(processo);
        processoDocumento.setAnexadoPorUsuario(usuario);
        processoDocumento.setAnexadoEm(LocalDateTime.now());

        processoDocumentoRepository.save(processoDocumento);

        return new ProcessoDocumentoResponse(
                documento.getId(),
                documento.getNomeOriginal(),
                documento.getMimeType(),
                documento.getOnedriveUrlDownload(),
                usuario.getId(),
                usuario.getNome(),
                processoDocumento.getAnexadoEm(),
                documento.getTipoDocumento() != null ? documento.getTipoDocumento().getNome() : null,
                documento.getPadraoArmazenamento() != null ? documento.getPadraoArmazenamento().getId() : null,
                documento.getPadraoArmazenamento() != null ? documento.getPadraoArmazenamento().getCaminho() : null
        );
    }

    public List<ProcessoDocumentoResponse> listarDocumentosDoProcesso(Long processoAppId) {
        authorizationUtil.verificarAcessoProcesso(processoAppId);
        return processoDocumentoRepository.findByProcessoAppId(processoAppId)
                .stream()
                .map(pd -> new ProcessoDocumentoResponse(
                        pd.getDocumento().getId(),
                        pd.getDocumento().getNomeOriginal(),
                        pd.getDocumento().getMimeType(),
                        pd.getDocumento().getOnedriveUrlDownload(),
                        pd.getAnexadoPorUsuario() != null ? pd.getAnexadoPorUsuario().getId() : null,
                        pd.getAnexadoPorUsuario() != null ? pd.getAnexadoPorUsuario().getNome() : null,
                        pd.getAnexadoEm(),
                        pd.getDocumento().getTipoDocumento() != null ? pd.getDocumento().getTipoDocumento().getNome() : null,
                        pd.getDocumento().getPadraoArmazenamento() != null ? pd.getDocumento().getPadraoArmazenamento().getId() : null,
                        pd.getDocumento().getPadraoArmazenamento() != null ? pd.getDocumento().getPadraoArmazenamento().getCaminho() : null
                ))
                .toList();
    }
}
