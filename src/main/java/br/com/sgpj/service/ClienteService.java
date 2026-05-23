package br.com.sgpj.service;

import br.com.sgpj.adapter.database.ClientePersistenciaAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.OneDriveItemResponse;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.ClienteJaExistenteException;
import br.com.sgpj.mapper.ClienteMapper;
import br.com.sgpj.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteService {

    private final ClientePersistenciaAdapter clientePersistenciaAdapter;
    private final ClienteRepository clienteRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final ContatoRepository contatoRepository;
    private final PadraoCaminhoArmazenamentoRepository padraoCaminhoArmazenamentoRepository;
    private final DocumentoRepository documentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    private final OneDriveService oneDriveService;

    public ClienteService(ClientePersistenciaAdapter clientePersistenciaAdapter,
                          ClienteRepository clienteRepository,
                          StatusClienteRepository statusClienteRepository,
                          EnderecoRepository enderecoRepository,
                          ContatoRepository contatoRepository,
                          PadraoCaminhoArmazenamentoRepository padraoCaminhoArmazenamentoRepository,
                          DocumentoRepository documentoRepository,
                          TipoDocumentoRepository tipoDocumentoRepository,
                          UsuarioRepository usuarioRepository,
                          UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter,
                          OneDriveService oneDriveService) {
        this.clientePersistenciaAdapter = clientePersistenciaAdapter;
        this.clienteRepository = clienteRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.contatoRepository = contatoRepository;
        this.padraoCaminhoArmazenamentoRepository = padraoCaminhoArmazenamentoRepository;
        this.documentoRepository = documentoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
        this.oneDriveService = oneDriveService;
    }

    public List<ClienteListarResponse> listarTodos() {
        return clientePersistenciaAdapter.listarTodos()
                .stream()
                .map(ClienteMapper::toListarDto)
                .toList();
    }

    public ClienteDetalhesResponse buscarPorId(Long id) {
        return ClienteMapper.toDetalhesDto(clientePersistenciaAdapter.buscarPorId(id));
    }

    public List<ClienteListarResponse> listarPorStatus(String status) {
        return clientePersistenciaAdapter.listarPorStatus(status)
                .stream()
                .map(ClienteMapper::toListarDto)
                .toList();
    }

    public List<ClienteListarResponse> pesquisarPorNomeOuCpfCnpj(String pesquisa) {
        return clientePersistenciaAdapter.pesquisarPorNomeOuCpfCnpj(pesquisa)
                .stream()
                .map(ClienteMapper::toListarDto)
                .toList();
    }

    @Transactional
    public ClienteDetalhesResponse criar(ClienteCriacaoRequest request) {
        // Verifica se já existe cliente com o mesmo CPF/CNPJ
        if (clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new ClienteJaExistenteException(request.cpfCnpj());
        }

        // Busca o status do cliente
        StatusCliente statusCliente = statusClienteRepository.findById(request.statusClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Status do cliente não encontrado com id: " + request.statusClienteId()));

        // Cria e salva o endereço, se fornecido
        Endereco endereco = null;
        if (request.endereco() != null) {
            endereco = new Endereco();
            endereco.setCep(request.endereco().cep());
            endereco.setLogradouro(request.endereco().logradouro());
            endereco.setNumero(request.endereco().numero());
            endereco.setBairro(request.endereco().bairro());
            endereco.setCidade(request.endereco().cidade());
            endereco.setEstado(request.endereco().estado());
            endereco.setComplemento(request.endereco().complemento());
            endereco = enderecoRepository.save(endereco);
        }

        // Cria o cliente
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial(request.nomeRazaoSocial());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setRg(request.rg());
        cliente.setStatusCliente(statusCliente);
        cliente.setEndereco(endereco);
        cliente.setDataHoraEnvioFormulario(LocalDateTime.now());

        cliente = clientePersistenciaAdapter.salvar(cliente);

        // Salva os contatos, se fornecidos
        if (request.contatos() != null && !request.contatos().isEmpty()) {
            Cliente clienteRef = cliente;
            List<Contato> contatos = request.contatos().stream()
                    .map(c -> {
                        Contato contato = new Contato();
                        contato.setNome(c.nome());
                        contato.setEmail(c.email());
                        contato.setTelefone(c.telefone());
                        contato.setProfissao(c.profissao());
                        contato.setPrincipal(c.principal());
                        contato.setCliente(clienteRef);
                        return contato;
                    })
                    .toList();
            contatoRepository.saveAll(contatos);
            cliente.setContatos(contatos);
        }

        // Cria a pasta no OneDrive com o formato "nome-cpfcnpj"
        String nomePasta = gerarNomePasta(request.nomeRazaoSocial(), request.cpfCnpj());
        OneDriveItemResponse pastaResponse = oneDriveService.criarPasta(nomePasta);

        // Salva o caminho na tabela padrao_caminho_armazenamento
        PadraoCaminhoArmazenamento caminhoArmazenamento = new PadraoCaminhoArmazenamento();
        caminhoArmazenamento.setNome(nomePasta);
        caminhoArmazenamento.setCaminhoBase(pastaResponse.caminho() != null ? pastaResponse.caminho() : "sgpj/usuarios/" + nomePasta);
        caminhoArmazenamento = padraoCaminhoArmazenamentoRepository.save(caminhoArmazenamento);

        // Salva o registro na tabela de documentos com a referência ao caminho
        Usuario usuarioLogado = obterUsuarioLogado();

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findByNomeIgnoreCase("OUTROS")
                .orElseThrow(() -> new IllegalArgumentException("Tipo de documento 'OUTROS' não encontrado"));

        Documento documento = new Documento();
        documento.setCriadoPorUsuario(usuarioLogado);
        documento.setTipoDocumento(tipoDocumento);
        documento.setPadraoCaminhoArmazenamento(caminhoArmazenamento);
        documento.setNomeOriginal(nomePasta);
        documento.setMimeType("application/folder");
        documento.setTamanhoBytes(0L);
        documento.setOnedriveItemId(pastaResponse.id());
        documento.setOnedriveUrlDownload(pastaResponse.urlDownload());
        documento.setOnedriveUrlThumbnail(pastaResponse.thumbnailUrl());
        documentoRepository.save(documento);

        return ClienteMapper.toDetalhesDto(clientePersistenciaAdapter.buscarPorId(cliente.getId()));
    }

    private String gerarNomePasta(String nome, String cpfCnpj) {
        String nomeFormatado = nome.trim()
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9À-ÿ\\s]", "")
                .replaceAll("\\s+", "-");
        return nomeFormatado + "-" + cpfCnpj;
    }

    private Usuario obterUsuarioLogado() {
        String email = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário logado não encontrado"));
    }
}
