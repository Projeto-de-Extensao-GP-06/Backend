package br.com.sgpj.service;

import br.com.sgpj.adapter.database.ClientePersistenciaAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.OneDriveItemResponse;
import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
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

    @Transactional
    public ClienteDetalhesResponse atualizar(Long id, ClienteAtualizacaoRequest request) {
        Cliente cliente = clientePersistenciaAdapter.buscarPorId(id);

        // Verifica se o CPF/CNPJ mudou e se já pertence a outro cliente
        if (!cliente.getCpfCnpj().equals(request.cpfCnpj()) && clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new ClienteJaExistenteException(request.cpfCnpj());
        }

        // Detecta se houve mudança no nome ou cpf/cnpj para renomear pasta
        boolean nomeAlterado = !cliente.getNomeRazaoSocial().equals(request.nomeRazaoSocial());
        boolean cpfCnpjAlterado = !cliente.getCpfCnpj().equals(request.cpfCnpj());
        String nomePastaAntiga = gerarNomePasta(cliente.getNomeRazaoSocial(), cliente.getCpfCnpj());

        // Atualiza os dados do cliente
        cliente.setNomeRazaoSocial(request.nomeRazaoSocial());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setRg(request.rg());

        // Atualiza o status
        StatusCliente statusCliente = statusClienteRepository.findById(request.statusClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Status do cliente não encontrado com id: " + request.statusClienteId()));
        cliente.setStatusCliente(statusCliente);

        // Atualiza o endereço
        if (request.endereco() != null) {
            Endereco endereco = cliente.getEndereco();
            if (endereco == null) {
                endereco = new Endereco();
            }
            endereco.setCep(request.endereco().cep());
            endereco.setLogradouro(request.endereco().logradouro());
            endereco.setNumero(request.endereco().numero());
            endereco.setBairro(request.endereco().bairro());
            endereco.setCidade(request.endereco().cidade());
            endereco.setEstado(request.endereco().estado());
            endereco.setComplemento(request.endereco().complemento());
            endereco = enderecoRepository.save(endereco);
            cliente.setEndereco(endereco);
        }

        // Atualiza contatos - remove os antigos e salva os novos
        if (request.contatos() != null) {
            if (cliente.getContatos() != null && !cliente.getContatos().isEmpty()) {
                contatoRepository.deleteAll(cliente.getContatos());
            }
            Cliente clienteRef = cliente;
            List<Contato> novosContatos = request.contatos().stream()
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
            contatoRepository.saveAll(novosContatos);
            cliente.setContatos(novosContatos);
        }

        clientePersistenciaAdapter.salvar(cliente);

        // Se o nome ou CPF/CNPJ mudou, renomeia a pasta no OneDrive
        if (nomeAlterado || cpfCnpjAlterado) {
            String nomePastaNova = gerarNomePasta(request.nomeRazaoSocial(), request.cpfCnpj());
            oneDriveService.renomearPasta(nomePastaAntiga, nomePastaNova);

            // Atualiza o registro na tabela padrao_caminho_armazenamento
            padraoCaminhoArmazenamentoRepository.findByNome(nomePastaAntiga)
                    .ifPresent(caminho -> {
                        caminho.setNome(nomePastaNova);
                        caminho.setCaminhoBase("sgpj/usuarios/" + nomePastaNova);
                        padraoCaminhoArmazenamentoRepository.save(caminho);

                        // Atualiza o nome original no documento associado
                        documentoRepository.findByPadraoCaminhoArmazenamento(caminho)
                                .ifPresent(doc -> {
                                    doc.setNomeOriginal(nomePastaNova);
                                    documentoRepository.save(doc);
                                });
                    });
        }

        return ClienteMapper.toDetalhesDto(clientePersistenciaAdapter.buscarPorId(id));
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
