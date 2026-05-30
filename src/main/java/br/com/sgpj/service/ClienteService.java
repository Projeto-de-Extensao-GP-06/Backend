package br.com.sgpj.service;

import br.com.sgpj.adapter.database.ClientePersistenciaAdapter;
import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Endereco;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.exception.ClienteJaExistenteException;
import br.com.sgpj.mapper.ClienteMapper;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClienteService {

    private final ClientePersistenciaAdapter clientePersistenciaAdapter;
    private final ClienteRepository clienteRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final ContatoRepository contatoRepository;
    private final OneDriveService oneDriveService;

    public ClienteService(ClientePersistenciaAdapter clientePersistenciaAdapter,
                          ClienteRepository clienteRepository,
                          StatusClienteRepository statusClienteRepository,
                          EnderecoRepository enderecoRepository,
                          ContatoRepository contatoRepository,
                          OneDriveService oneDriveService) {
        this.clientePersistenciaAdapter = clientePersistenciaAdapter;
        this.clienteRepository = clienteRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.contatoRepository = contatoRepository;
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

        return ClienteMapper.toDetalhesDto(clientePersistenciaAdapter.buscarPorId(cliente.getId()));
    }

    @Transactional
    public ClienteDetalhesResponse atualizar(Long id, ClienteAtualizacaoRequest request) {
        Cliente cliente = clientePersistenciaAdapter.buscarPorId(id);

        // Verifica se o CPF/CNPJ mudou e se já pertence a outro cliente
        if (!cliente.getCpfCnpj().equals(request.cpfCnpj()) && clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
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

        return ClienteMapper.toDetalhesDto(clientePersistenciaAdapter.buscarPorId(id));
    }

    @Transactional
    public void deletar(Long id) {
        Cliente cliente = clientePersistenciaAdapter.buscarPorId(id);

        // Remove contatos
        if (cliente.getContatos() != null && !cliente.getContatos().isEmpty()) {
            contatoRepository.deleteAll(cliente.getContatos());
        }

        // Remove pastas/caminhos associados usando a regra atual da main.
        if (cliente.getCaminhos() != null && !cliente.getCaminhos().isEmpty()) {
            List<Long> caminhoIds = new ArrayList<>(cliente.getCaminhos().stream()
                    .map(CaminhoPadraoDeArmazenamento::getId)
                    .toList());
            caminhoIds.forEach(oneDriveService::excluirPasta);
        }

        // Remove o cliente
        clientePersistenciaAdapter.deletar(id);
    }
}
