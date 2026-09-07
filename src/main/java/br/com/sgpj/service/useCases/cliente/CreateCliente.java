package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Endereco;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.exception.ClienteJaExistenteException;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.exception.StatusClienteNaoEncontradoException;
import br.com.sgpj.mapper.ClienteMapper;
import br.com.sgpj.mapper.ContatoMapper;
import br.com.sgpj.mapper.EnderecoMapper;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CreateCliente {

    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final ContatoRepository contatoRepository;

    public CreateCliente(ClienteRepository clienteRepository, EnderecoRepository enderecoRepository, StatusClienteRepository statusClienteRepository, ContatoRepository contatoRepository) {
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.contatoRepository = contatoRepository;
    }

    @Transactional
    public ClienteDetalhesResponse criarCliente(ClienteCriacaoRequest request) {
        if (clienteRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new ClienteJaExistenteException(request.cpfCnpj());
        }

        StatusCliente statusCliente = statusClienteRepository.findById(request.statusClienteId())
                .orElseThrow(() -> new StatusClienteNaoEncontradoException(request.statusClienteId()));

        Endereco endereco = request.endereco() != null ? montarEndereco(request) : null;
        Cliente clienteSalvo = montarCliente(request, statusCliente, endereco);

        if (request.contatos() != null && !request.contatos().isEmpty()) {
            clienteSalvo.setContatos(salvarContatos(request, clienteSalvo));
        }

        Long clienteId = clienteSalvo.getId();
        return ClienteMapper.toDetalhesDto(clienteRepository.findByIdComDetalhes(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId)));
    }

    public Endereco montarEndereco(ClienteCriacaoRequest request) {
        return enderecoRepository.save(EnderecoMapper.toEntity(request.endereco()));
    }

    public Cliente montarCliente(ClienteCriacaoRequest request, StatusCliente statusCliente, Endereco endereco) {
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial(request.nomeRazaoSocial());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setPF(request.isPF());
        cliente.setRg(request.rg());
        cliente.setStatusCliente(statusCliente);
        cliente.setEndereco(endereco);
        cliente.setDataHoraEnvioFormulario(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

    public List<Contato> salvarContatos(ClienteCriacaoRequest request, Cliente clienteRef) {
        return contatoRepository.saveAll(request.contatos().stream()
                .map(contatoRequest -> ContatoMapper.toEntity(contatoRequest, clienteRef))
                .toList());
    }
}
