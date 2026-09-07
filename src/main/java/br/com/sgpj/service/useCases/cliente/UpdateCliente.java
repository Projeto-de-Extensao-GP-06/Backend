package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
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

import java.util.List;
import java.util.Objects;

@Component
public class UpdateCliente {

    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final EnderecoRepository enderecoRepository;

    public UpdateCliente(ClienteRepository clienteRepository, ContatoRepository contatoRepository, StatusClienteRepository statusClienteRepository, EnderecoRepository enderecoRepository) {
        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.enderecoRepository = enderecoRepository;
    }

    @Transactional
    public ClienteDetalhesResponse atualizarCliente(Long id, ClienteAtualizacaoRequest request) {
        Cliente cliente = buscarCliente(id);
        validarCpfCnpj(request.cpfCnpj(), cliente.getCpfCnpj());

        cliente.setNomeRazaoSocial(request.nomeRazaoSocial());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setRg(request.rg());
        cliente.setStatusCliente(buscarStatusCliente(request.statusClienteId()));

        if (request.endereco() != null) {
            cliente.setEndereco(montarEndereco(request, cliente.getEndereco()));
        }

        if (request.contatos() != null) {
            atualizarContatos(cliente, request.contatos());
        }

        clienteRepository.save(cliente);
        return ClienteMapper.toDetalhesDto(buscarCliente(id));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    private void validarCpfCnpj(String cpfCnpjNovo, String cpfCnpjAtual) {
        if (!Objects.equals(cpfCnpjAtual, cpfCnpjNovo) && clienteRepository.existsByCpfCnpj(cpfCnpjNovo)) {
            throw new ClienteJaExistenteException(cpfCnpjNovo);
        }
    }

    private StatusCliente buscarStatusCliente(Long statusClienteId) {
        return statusClienteRepository.findById(statusClienteId)
                .orElseThrow(() -> new StatusClienteNaoEncontradoException(statusClienteId));
    }

    private Endereco montarEndereco(ClienteAtualizacaoRequest request, Endereco enderecoExistente) {
        Endereco endereco = EnderecoMapper.toEntity(request.endereco(), enderecoExistente);
        return enderecoRepository.save(endereco);
    }

    private void atualizarContatos(Cliente cliente, List<ClienteAtualizacaoRequest.ContatoRequest> contatosRequest) {
        if (cliente.getContatos() != null && !cliente.getContatos().isEmpty()) {
            contatoRepository.deleteAll(cliente.getContatos());
        }

        List<Contato> contatos = contatosRequest.stream()
                .map(contatoRequest -> ContatoMapper.toEntity(contatoRequest, cliente))
                .toList();

        cliente.setContatos(contatoRepository.saveAll(contatos));
    }
}
