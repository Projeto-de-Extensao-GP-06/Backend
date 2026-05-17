package br.com.sgpj.service;

import br.com.sgpj.adapter.database.ClientePersistenciaAdapter;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.mapper.ClienteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClientePersistenciaAdapter clientePersistenciaAdapter;

    public ClienteService(ClientePersistenciaAdapter clientePersistenciaAdapter) {
        this.clientePersistenciaAdapter = clientePersistenciaAdapter;
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
}
