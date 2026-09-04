package br.com.sgpj.service;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.SolicitacaoDuvida;
import br.com.sgpj.entity.SolicitacaoStatus;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.SolicitacaoDuvidaMapper;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitacaoDuvidaService {

    private static final String STATUS_LEAD = "LEAD";
    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_INATIVO = "INATIVO";
    private static final String OBSERVACAO_SOLICITACAO_PREFIXO = "Solicitação enviada pelo formulário público. Estado: ";

    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final ProcessoAppRepository processoAppRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final ProcessoService processoService;

    public SolicitacaoDuvidaService(ClienteRepository clienteRepository,
                                    ContatoRepository contatoRepository,
                                    ProcessoAppRepository processoAppRepository,
                                    StatusClienteRepository statusClienteRepository,
                                    ProcessoService processoService) {
        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
        this.processoAppRepository = processoAppRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.processoService = processoService;
    }

    @Transactional
    public SolicitacaoDuvidaDetalhesResponse criar(SolicitacaoDuvidaCriacaoRequest request) {
        String estado = request.estado().trim().toUpperCase();
        String numeroProcesso = normalizarOpcional(request.numeroProcesso());

        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial(request.nomeCompleto().trim());
        cliente.setPF(request.isPF());
        cliente.setStatusCliente(buscarStatus(STATUS_LEAD));
        cliente.setDataHoraEnvioFormulario(LocalDateTime.now());
        cliente = clienteRepository.save(cliente);

        Contato contato = new Contato();
        contato.setNome(request.nomeCompleto().trim());
        contato.setEmail(request.email().trim());
        contato.setTelefone(request.telefone().trim());
        contato.setPrincipal(true);
        contato.setCliente(cliente);
        contato = contatoRepository.save(contato);
        cliente.setContatos(List.of(contato));

        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setCliente(cliente);
        processoApp.setDescricaoCaso(request.descricao().trim());
        processoApp.setObservacao(OBSERVACAO_SOLICITACAO_PREFIXO + estado);
        processoApp.setDataHoraInicio(cliente.getDataHoraEnvioFormulario());
        processoApp.setUltimaAtualizacao(cliente.getDataHoraEnvioFormulario());
        processoApp.setAtivo(false);

        if (numeroProcesso != null) {
            processoApp.setProcessoApi(buscarProcessoOficial(numeroProcesso, estado));
        }

        return SolicitacaoDuvidaMapper.toDetalhesResponse(toSolicitacao(processoAppRepository.save(processoApp)));
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoDuvidaListarResponse> listarPendentes() {
        return processoAppRepository.findByCliente_StatusCliente_NomeIgnoreCaseOrderByDataHoraInicioDesc(STATUS_LEAD)
                .stream()
                .map(this::toSolicitacao)
                .map(SolicitacaoDuvidaMapper::toListarResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoDuvidaDetalhesResponse buscarPorId(Long id) {
        return SolicitacaoDuvidaMapper.toDetalhesResponse(buscarSolicitacao(id));
    }

    @Transactional
    public SolicitacaoClientePreenchimentoResponse aceitar(Long id) {
        ProcessoApp processoApp = buscarProcessoSolicitacao(id);
        Cliente cliente = processoApp.getCliente();
        cliente.setStatusCliente(buscarStatus(STATUS_ATIVO));
        cliente.setDataHoraTransformacaoEmCliente(LocalDateTime.now());
        clienteRepository.save(cliente);
        processoApp.setAtivo(true);
        processoApp.setUltimaAtualizacao(LocalDateTime.now());
        processoAppRepository.save(processoApp);

        return SolicitacaoDuvidaMapper.toClientePreenchimentoResponse(toSolicitacao(processoApp));
    }

    @Transactional
    public void negar(Long id) {
        ProcessoApp processoApp = buscarProcessoSolicitacao(id);
        Cliente cliente = processoApp.getCliente();
        cliente.setStatusCliente(buscarStatus(STATUS_INATIVO));
        cliente.setDataHoraTransformacaoEmCliente(LocalDateTime.now());
        clienteRepository.save(cliente);
        processoApp.setAtivo(false);
        processoApp.setUltimaAtualizacao(LocalDateTime.now());
        processoAppRepository.save(processoApp);
    }

    private SolicitacaoDuvida buscarSolicitacao(Long id) {
        return toSolicitacao(buscarProcessoSolicitacao(id));
    }

    private ProcessoApp buscarProcessoSolicitacao(Long id) {
        return processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Solicitação não encontrada para o ID: " + id));
    }

    private StatusCliente buscarStatus(String nome) {
        return statusClienteRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Status de cliente não encontrado: " + nome));
    }

    private SolicitacaoDuvida toSolicitacao(ProcessoApp processoApp) {
        Cliente cliente = processoApp.getCliente();
        Contato contato = contatoPrincipal(cliente);
        ProcessoApi processoApi = processoApp.getProcessoApi();

        SolicitacaoDuvida solicitacao = new SolicitacaoDuvida();
        solicitacao.setId(processoApp.getId());
        solicitacao.setClienteId(cliente.getId());
        solicitacao.setNomeCompleto(cliente.getNomeRazaoSocial());
        solicitacao.setEmail(contato != null ? contato.getEmail() : null);
        solicitacao.setTelefone(contato != null ? contato.getTelefone() : null);
        solicitacao.setEstado(extrairEstado(processoApp.getObservacao()));
        solicitacao.setNumeroProcesso(processoApi != null ? processoApi.getNumeroCnj() : null);
        solicitacao.setDescricao(processoApp.getDescricaoCaso());
        solicitacao.setAceitouTermos(true);
        solicitacao.setDataSolicitacao(processoApp.getDataHoraInicio());
        solicitacao.setDataResposta(cliente.getDataHoraTransformacaoEmCliente());
        solicitacao.setStatus(toSolicitacaoStatus(cliente));
        solicitacao.setProcessoApi(processoApi);
        return solicitacao;
    }

    private Contato contatoPrincipal(Cliente cliente) {
        if (cliente.getContatos() == null || cliente.getContatos().isEmpty()) {
            return null;
        }

        return cliente.getContatos().stream()
                .filter(contato -> Boolean.TRUE.equals(contato.getPrincipal()))
                .findFirst()
                .orElse(cliente.getContatos().getFirst());
    }

    private SolicitacaoStatus toSolicitacaoStatus(Cliente cliente) {
        String status = cliente.getStatusCliente() != null ? cliente.getStatusCliente().getNome() : null;
        if (STATUS_ATIVO.equalsIgnoreCase(status)) {
            return SolicitacaoStatus.ACEITA;
        }
        if (STATUS_INATIVO.equalsIgnoreCase(status)) {
            return SolicitacaoStatus.NEGADA;
        }
        return SolicitacaoStatus.PENDENTE;
    }

    private String extrairEstado(String observacao) {
        if (observacao == null || !observacao.startsWith(OBSERVACAO_SOLICITACAO_PREFIXO)) {
            return null;
        }
        return observacao.substring(OBSERVACAO_SOLICITACAO_PREFIXO.length());
    }

    private ProcessoApi buscarProcessoOficial(String numeroProcesso, String estado) {
        try {
            return processoService.buscarDadosOficiais(numeroProcesso, new SiglaTribunal(null, "TJ" + estado));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
