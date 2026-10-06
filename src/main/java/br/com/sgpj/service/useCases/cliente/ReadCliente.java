package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.dto.response.Cliente.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.Cliente.ClienteListarResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.mapper.ClienteMapper;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.security.AuthorizationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReadCliente {

    private final ClienteRepository clienteRepository;
    private final AuthorizationUtil authorizationUtil;


    public ReadCliente(ClienteRepository clienteRepository, AuthorizationUtil authorizationUtil) {
        this.clienteRepository = clienteRepository;
        this.authorizationUtil = authorizationUtil;
    }

    public List<ClienteListarResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteMapper::toListarDto)
                .toList();
    }

    public ClienteDetalhesResponse buscarPorId(Long id) {
        authorizationUtil.verificarAcessoCliente(id);
        Cliente cliente = clienteRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
        return ClienteMapper.toDetalhesDto(cliente);
    }

    public List<ClienteListarResponse> listarPorStatus(String status) {
        return clienteRepository.findByStatusCliente_NomeIgnoreCase(status.trim())
                .stream()
                .map(ClienteMapper::toListarDto)
                .toList();
    }

    public List<ClienteListarResponse> pesquisarPorNomeOuCpfCnpj(String pesquisa) {
        String termo = pesquisa.trim();
        return clienteRepository.findByNomeRazaoSocialContainingIgnoreCaseOrCpfCnpjContaining(termo, termo)
                .stream()
                .map(ClienteMapper::toListarDto)
                .toList();
    }

    public Page<ClienteListarResponse> pesquisarComFiltros(String termo, String status, String tipoPessoa, Pageable pageable) {
        return clienteRepository.findAll(ClienteSpecification.comFiltros(termo, status, tipoPessoa), pageable)
                .map(ClienteMapper::toListarDto);
    }

}
