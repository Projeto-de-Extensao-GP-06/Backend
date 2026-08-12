package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.specification.ClienteSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientePersistenciaAdapter {

    private final ClienteRepository clienteRepository;

    public ClientePersistenciaAdapter(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    public List<Cliente> listarPorStatus(String status) {
        return clienteRepository.findByStatusCliente_NomeIgnoreCase(status.trim());
    }

    public List<Cliente> pesquisarPorNomeOuCpfCnpj(String pesquisa) {
        String termo = pesquisa.trim();
        return clienteRepository.findByNomeRazaoSocialContainingIgnoreCaseOrCpfCnpjContaining(termo, termo);
    }

    public Page<Cliente> pesquisarComFiltros(String termo, String status, String tipoPessoa, Pageable pageable) {
        return clienteRepository.findAll(ClienteSpecification.comFiltros(termo, status, tipoPessoa), pageable);
    }

    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deletar(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }
}