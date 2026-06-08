package br.com.sgpj.service;

import br.com.sgpj.adapter.database.ClientePersistenciaAdapter;
import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Endereco;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.exception.ClienteJaExistenteException;
import br.com.sgpj.mapper.ClienteMapper;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ClienteService {

    private final ClientePersistenciaAdapter clientePersistenciaAdapter;
    private final ClienteRepository clienteRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final ContatoRepository contatoRepository;
    private final CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    private final DocumentoRepository documentoRepository;
    private final ProcessoAppRepository processoAppRepository;
    private final HistoricoProcessoRepository historicoProcessoRepository;
    private final ProcessoDocumentoRepository processoDocumentoRepository;
    private final OneDriveService oneDriveService;

    public ClienteService(ClientePersistenciaAdapter clientePersistenciaAdapter,
                          ClienteRepository clienteRepository,
                          StatusClienteRepository statusClienteRepository,
                          EnderecoRepository enderecoRepository,
                          ContatoRepository contatoRepository,
                          CaminhoPadraoDeArmazenamentoRepository caminhoRepository,
                          DocumentoRepository documentoRepository,
                          ProcessoAppRepository processoAppRepository,
                          HistoricoProcessoRepository historicoProcessoRepository,
                          ProcessoDocumentoRepository processoDocumentoRepository,
                          OneDriveService oneDriveService) {
        this.clientePersistenciaAdapter = clientePersistenciaAdapter;
        this.clienteRepository = clienteRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.contatoRepository = contatoRepository;
        this.caminhoRepository = caminhoRepository;
        this.documentoRepository = documentoRepository;
        this.processoAppRepository = processoAppRepository;
        this.historicoProcessoRepository = historicoProcessoRepository;
        this.processoDocumentoRepository = processoDocumentoRepository;
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
            List<Contato> contatos = new ArrayList<>(request.contatos().stream()
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
                    .toList());
            contatoRepository.saveAll(contatos);
            cliente.setContatos(contatos);
        }

        return ClienteMapper.toDetalhesDto(clientePersistenciaAdapter.buscarPorId(cliente.getId()));
    }

    @Transactional
    public ClienteDetalhesResponse atualizar(Long id, ClienteAtualizacaoRequest request) {
        Cliente cliente = clientePersistenciaAdapter.buscarPorId(id);

        // Verifica se o CPF/CNPJ mudou e se já pertence a outro cliente
        if (!Objects.equals(cliente.getCpfCnpj(), request.cpfCnpj()) && clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new ClienteJaExistenteException(request.cpfCnpj());
        }

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
                cliente.setContatos(new ArrayList<>());
            }
            List<Contato> novosContatos = new ArrayList<>(request.contatos().stream()
                    .map(c -> {
                        Contato contato = new Contato();
                        contato.setNome(c.nome());
                        contato.setEmail(c.email());
                        contato.setTelefone(c.telefone());
                        contato.setProfissao(c.profissao());
                        contato.setPrincipal(c.principal());
                        contato.setCliente(cliente);
                        return contato;
                    })
                    .toList());
            contatoRepository.saveAll(novosContatos);
            cliente.setContatos(novosContatos);
        }

        clientePersistenciaAdapter.salvar(cliente);

        return ClienteMapper.toDetalhesDto(clientePersistenciaAdapter.buscarPorId(id));
    }

    @Transactional
    public void deletar(Long id) {
        Cliente cliente = clientePersistenciaAdapter.buscarPorId(id);
        Endereco endereco = cliente.getEndereco();

        List<CaminhoPadraoDeArmazenamento> caminhos = new ArrayList<>(caminhoRepository.findByClienteId(id));
        Set<Long> caminhoIds = new HashSet<>(caminhos.stream()
                .map(CaminhoPadraoDeArmazenamento::getId)
                .toList());
        List<Documento> documentos = new ArrayList<>();
        Set<Long> documentoIds = new HashSet<>();

        // Remove dependências dos processos do cliente
        List<ProcessoApp> processos = processoAppRepository.findByCliente(cliente);
        for (ProcessoApp processo : processos) {
            List<ProcessoDocumento> vinculosDocumentos = processoDocumentoRepository.findByProcessoAppId(processo.getId());
            for (ProcessoDocumento vinculoDocumento : vinculosDocumentos) {
                Documento documento = vinculoDocumento.getDocumento();
                if (documento != null && documento.getId() != null && documentoIds.add(documento.getId())) {
                    documentos.add(documento);
                }
                if (documento != null && documento.getPadraoArmazenamento() != null
                        && documento.getPadraoArmazenamento().getId() != null
                        && caminhoIds.add(documento.getPadraoArmazenamento().getId())) {
                    caminhos.add(documento.getPadraoArmazenamento());
                }
            }
            if (!vinculosDocumentos.isEmpty()) {
                processoDocumentoRepository.deleteAll(vinculosDocumentos);
            }
            historicoProcessoRepository.deleteByProcessoApp(processo);
        }
        if (!processos.isEmpty()) {
            processoAppRepository.deleteAll(processos);
        }

        for (CaminhoPadraoDeArmazenamento caminho : caminhos) {
            for (Documento documento : documentoRepository.findByPadraoArmazenamentoId(caminho.getId())) {
                if (documento.getId() != null && documentoIds.add(documento.getId())) {
                    documentos.add(documento);
                }
            }
        }

        // Remove vínculos e documentos associados aos caminhos do cliente
        for (Documento documento : documentos) {
            List<ProcessoDocumento> vinculosDocumentos = processoDocumentoRepository.findByDocumentoId(documento.getId());
            if (!vinculosDocumentos.isEmpty()) {
                processoDocumentoRepository.deleteAll(vinculosDocumentos);
            }
        }
        if (!documentos.isEmpty()) {
            documentoRepository.deleteAll(documentos);
        }

        // Remove contatos
        if (cliente.getContatos() != null && !cliente.getContatos().isEmpty()) {
            contatoRepository.deleteAll(cliente.getContatos());
        }

        // Remove pastas/caminhos associados usando a regra atual da main.
        if (!caminhos.isEmpty()) {
            List<Long> idsCaminhosParaExcluir = new ArrayList<>(caminhos.stream()
                    .map(CaminhoPadraoDeArmazenamento::getId)
                    .toList());
            idsCaminhosParaExcluir.forEach(oneDriveService::excluirPasta);
        }

        // Remove o cliente
        clientePersistenciaAdapter.deletar(id);

        if (endereco != null) {
            enderecoRepository.delete(endereco);
        }
    }

}
