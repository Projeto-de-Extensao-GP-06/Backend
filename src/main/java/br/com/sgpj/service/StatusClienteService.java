package br.com.sgpj.service;

import br.com.sgpj.dto.response.StatusClienteResponse;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.repository.StatusClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusClienteService {

    private final StatusClienteRepository statusClienteRepository;

    public StatusClienteService(StatusClienteRepository statusClienteRepository) {
        this.statusClienteRepository = statusClienteRepository;
    }

    public List<StatusClienteResponse> listarStatusClientes() {
        return statusClienteRepository.findAll()
                .stream()
                .map(statusCliente -> new StatusClienteResponse(statusCliente.getId(), statusCliente.getNome()))
                .toList();
    }
}
