package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoAceiteRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Endereco;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.mapper.ProcessoAppToSolicitacaoDuvidaMapper;
import br.com.sgpj.mapper.SolicitacaoDuvidaMapper;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.PrioridadeRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import br.com.sgpj.service.useCases.onedrive.GarantirPastasClienteOneDrive;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Component
public class AceitarSolicitacao {

    private final ProcessoAppRepository processoAppRepository;
    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final EnderecoRepository enderecoRepository;
    private final PrioridadeRepository prioridadeRepository;
    private final FaseProcessoRepository faseProcessoRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final GarantirPastasClienteOneDrive garantirPastasClienteOneDrive;
    private final RegistroHistoricoProcesso registroHistorico;

    public AceitarSolicitacao(ProcessoAppRepository processoAppRepository,
                              ClienteRepository clienteRepository,
                              ContatoRepository contatoRepository,
                              EnderecoRepository enderecoRepository,
                              PrioridadeRepository prioridadeRepository,
                              FaseProcessoRepository faseProcessoRepository,
                              StatusClienteRepository statusClienteRepository,
                              GarantirPastasClienteOneDrive garantirPastasClienteOneDrive,
                              RegistroHistoricoProcesso registroHistorico) {
        this.processoAppRepository = processoAppRepository;
        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
        this.enderecoRepository = enderecoRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.garantirPastasClienteOneDrive = garantirPastasClienteOneDrive;
        this.registroHistorico = registroHistorico;
    }

    @Transactional
    public SolicitacaoClientePreenchimentoResponse aceitar(Long id, SolicitacaoAceiteRequest request, Usuario usuarioResponsavel) {
        ProcessoApp processoApp = buscarProcessoSolicitacao(id);
        if (processoApp.getStatus() != StatusProcesso.SOLICITACAO) {
            throw new OperacaoInvalidaException("A solicitação já foi processada");
        }

        Cliente cliente = processoApp.getCliente();
        processoAppRepository.existsByCliente_IdAndIdNot(cliente.getId(), id);

        boolean clienteAtivo = cliente.getStatusCliente() != null
                && StatusClienteEnum.ATIVO.getNome().equalsIgnoreCase(cliente.getStatusCliente().getNome());
        StatusCliente statusAtivo = clienteAtivo ? cliente.getStatusCliente() : buscarStatus(StatusClienteEnum.ATIVO);
        Prioridade prioridade = buscarPrioridade(request.process().priority());
        FaseProcesso fase = buscarFase(request.process().phase());

        aplicarDadosCliente(cliente, request.client());
        aplicarDadosProcesso(processoApp, request.process(), prioridade, fase);
        if (processoApp.getUsuarioResponsavel() == null) {
            processoApp.setUsuarioResponsavel(usuarioResponsavel);
        }
        if (!clienteAtivo) {
            cliente.setStatusCliente(statusAtivo);
            cliente.setDataHoraTransformacaoEmCliente(LocalDateTime.now());
        }
        clienteRepository.save(cliente);

        processoApp.setStatus(StatusProcesso.ATIVO);
        processoApp.setUltimaAtualizacao(LocalDateTime.now());
        processoAppRepository.save(processoApp);
        registroHistorico.registrar(
                processoApp,
                usuarioResponsavel,
                RegistroHistoricoProcesso.ABERTURA,
                "Processo criado."
        );
        garantirPastasClienteOneDrive.garantir(cliente, id);

        return SolicitacaoDuvidaMapper.toClientePreenchimentoResponse(
                ProcessoAppToSolicitacaoDuvidaMapper.toSolicitacaoDuvida(processoApp),
                processoApp
        );
    }

    private void aplicarDadosCliente(Cliente cliente, SolicitacaoAceiteRequest.Client request) {
        boolean pessoaFisica = "PF".equals(request.type());
        if (cliente.getPF() == null) cliente.setPF(pessoaFisica);
        preencher(cliente.getNomeRazaoSocial(), pessoaFisica ? request.name() : request.companyName(), cliente::setNomeRazaoSocial);
        preencher(cliente.getCpfCnpj(), pessoaFisica ? normalizarDocumento(request.cpf()) : normalizarDocumento(request.cnpj()), cliente::setCpfCnpj);
        if (pessoaFisica) preencher(cliente.getRg(), normalizarRg(request.rg()), cliente::setRg);

        aplicarEndereco(cliente, request.address());
        aplicarContato(cliente, request);
    }

    private void aplicarEndereco(Cliente cliente, SolicitacaoAceiteRequest.Address request) {
        Endereco endereco = cliente.getEndereco() == null ? new Endereco() : cliente.getEndereco();
        preencher(endereco.getCep(), request.cep().replaceAll("\\D", ""), endereco::setCep);
        preencher(endereco.getLogradouro(), request.logradouro(), endereco::setLogradouro);
        preencher(endereco.getNumero(), request.numero(), endereco::setNumero);
        preencher(endereco.getComplemento(), request.complemento(), endereco::setComplemento);
        preencher(endereco.getBairro(), request.bairro(), endereco::setBairro);
        preencher(endereco.getCidade(), request.cidade(), endereco::setCidade);
        preencher(endereco.getEstado(), request.estado().trim().toUpperCase(Locale.ROOT), endereco::setEstado);
        cliente.setEndereco(enderecoRepository.save(endereco));
    }

    private void aplicarContato(Cliente cliente, SolicitacaoAceiteRequest.Client request) {
        List<Contato> contatos = cliente.getContatos() == null ? new ArrayList<>() : new ArrayList<>(cliente.getContatos());
        Contato contato = contatos.stream()
                .filter(item -> Boolean.TRUE.equals(item.getPrincipal()))
                .findFirst()
                .orElseGet(() -> contatos.isEmpty() ? null : contatos.getFirst());

        SolicitacaoAceiteRequest.Contact contatoRequest = request.contacts() == null ? null : request.contacts().stream()
                .filter(item -> Boolean.TRUE.equals(item.isPrimary()))
                .findFirst()
                .orElse(request.contacts().isEmpty() ? null : request.contacts().getFirst());
        String nomeContato = contatoRequest != null ? contatoRequest.name() : request.name();
        String email = contatoRequest != null && hasValue(contatoRequest.email()) ? contatoRequest.email() : request.email();
        String telefone = contatoRequest != null && hasValue(contatoRequest.phone()) ? contatoRequest.phone() : request.phone();
        String profissao = contatoRequest != null && hasValue(contatoRequest.profession())
                ? contatoRequest.profession() : request.profession();

        if (contato == null) {
            contato = new Contato();
            contato.setCliente(cliente);
            contato.setPrincipal(true);
            contatos.add(contato);
            cliente.setContatos(contatos);
        }
        preencher(contato.getNome(), nomeContato, contato::setNome);
        preencher(contato.getEmail(), email, contato::setEmail);
        preencher(contato.getTelefone(), normalizarTelefone(telefone), contato::setTelefone);
        preencher(contato.getProfissao(), profissao, contato::setProfissao);
        contatoRepository.save(contato);
    }

    private void aplicarDadosProcesso(ProcessoApp processo,
                                      SolicitacaoAceiteRequest.Process request,
                                      Prioridade prioridade,
                                      FaseProcesso fase) {
        if (processo.getPrioridade() == null) processo.setPrioridade(prioridade);
        if (processo.getFaseProcesso() == null) processo.setFaseProcesso(fase);
        if (processo.getValorCausa() == null && request.value() != null) {
            processo.setValorCausa(request.value().doubleValue());
        }
        if (processo.getDataAudiencia() == null && request.hearingDate() != null && request.hearingTime() != null) {
            processo.setDataAudiencia(LocalDateTime.of(request.hearingDate(), request.hearingTime()));
        }
        if (hasValue(request.cnj())) aplicarCnj(processo, request.cnj());
    }

    private void aplicarCnj(ProcessoApp processo, String cnj) {
        ProcessoApi processoApi = processo.getProcessoApi();
        if (processoApi != null && hasValue(processoApi.getNumeroCnj())) return;
        if (processoApi != null && processoAppRepository.existsByProcessoApi_IdAndIdNot(processoApi.getId(), processo.getId())) {
            processoApi = null;
        }
        if (processoApi == null) processoApi = new ProcessoApi();
        processoApi.setNumeroCnj(cnj.replaceAll("\\D", ""));
        processo.setProcessoApi(processoApi);
    }

    private Prioridade buscarPrioridade(String valor) {
        String nome = switch (normalizarChave(valor)) {
            case "ALTA" -> "ALTA";
            case "MEDIA" -> "MEDIA";
            case "BAIXA" -> "BAIXA";
            default -> throw new IllegalArgumentException("Prioridade inválida");
        };
        return prioridadeRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Prioridade não encontrada: " + nome));
    }

    private FaseProcesso buscarFase(String valor) {
        String nome = switch (normalizarChave(valor)) {
            case "INICIAL" -> "INICIAL";
            case "CITACAO" -> "CITAÇÃO";
            case "SANEAMENTO" -> "SANEAMENTO";
            case "SENTENCA" -> "SENTENÇA";
            case "CUMPRIMENTO DE SENTENCA" -> "CUPRIMENTO DE SENTENÇA";
            case "PENHORA" -> "PENHORA";
            default -> throw new IllegalArgumentException("Fase do processo inválida");
        };
        return faseProcessoRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fase do processo não encontrada: " + nome));
    }

    private String normalizarChave(String valor) {
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

    private String normalizarDocumento(String valor) {
        return valor == null ? null : valor.toUpperCase(Locale.ROOT).replaceAll("[^0-9A-Z]", "");
    }

    private String normalizarRg(String valor) {
        return valor == null ? null : valor.replaceAll("[^0-9A-Za-z]", "");
    }

    private String normalizarTelefone(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private void preencher(String atual, String recebido, Consumer<String> setter) {
        if (!hasValue(atual) && hasValue(recebido)) setter.accept(recebido.trim());
    }

    private boolean hasValue(String valor) {
        return valor != null && !valor.isBlank();
    }

    private ProcessoApp buscarProcessoSolicitacao(Long id) {
        return processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Solicitação não encontrada para o ID: " + id));
    }

    private StatusCliente buscarStatus(StatusClienteEnum statusEnum) {
        return statusClienteRepository.findByNomeIgnoreCase(statusEnum.getNome())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Status de cliente não encontrado: " + statusEnum.getNome()));
    }
}
