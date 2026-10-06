package br.com.sgpj.service;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.Cliente.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.Cliente.ClienteListarResponse;
import br.com.sgpj.service.useCases.cliente.CreateCliente;
import br.com.sgpj.service.useCases.cliente.DeleteCliente;
import br.com.sgpj.service.useCases.cliente.ReadCliente;
import br.com.sgpj.service.useCases.cliente.UpdateCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ClienteService {

    private final CreateCliente criarClienteUseCase;
    private final ReadCliente readClienteUseCase;
    private final UpdateCliente updateClienteUseCase;
    private final DeleteCliente deleteCliente;


    public ClienteService(CreateCliente criarClienteUseCase, ReadCliente readClienteUseCase, UpdateCliente updateClienteUseCase, DeleteCliente deleteCliente) {
        this.criarClienteUseCase = criarClienteUseCase;
        this.readClienteUseCase = readClienteUseCase;
        this.updateClienteUseCase = updateClienteUseCase;
        this.deleteCliente = deleteCliente;
    }


    //CREATE OPERATIONS
    public ClienteDetalhesResponse criar(ClienteCriacaoRequest clienteRequest){
        return criarClienteUseCase.criarCliente(clienteRequest);
    }


    // READ OPERATIONS
    public List<ClienteListarResponse> listarTodos() {
        return readClienteUseCase.listarTodos();
    }

    public ClienteDetalhesResponse buscarPorId(Long id) {
        return readClienteUseCase.buscarPorId(id);
    }

    public List<ClienteListarResponse> listarPorStatus(String status) {
        return readClienteUseCase.listarPorStatus(status);
    }

    public List<ClienteListarResponse> pesquisarPorNomeOuCpfCnpj(String pesquisa) {
        String termo = pesquisa.trim();
        return readClienteUseCase.pesquisarPorNomeOuCpfCnpj(termo);
    }

    public Page<ClienteListarResponse> pesquisarComFiltros(String termo, String status, String tipoPessoa, Pageable pageable) {
        return readClienteUseCase.pesquisarComFiltros(termo, status, tipoPessoa, pageable);
    }


    //UPDATE OPERATIONS
    public ClienteDetalhesResponse atualizar(Long id, ClienteAtualizacaoRequest request) {
        return updateClienteUseCase.atualizarCliente(id, request);
    }

    //DELETE OPERATIONS
    public void deletar(Long id) {
        deleteCliente.deletarCliente(id);
    }
}
