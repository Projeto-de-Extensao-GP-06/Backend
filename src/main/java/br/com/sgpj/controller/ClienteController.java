package br.com.sgpj.controller;

import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.service.ClienteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ClienteListarResponse>> listarTodos() {
        List<ClienteListarResponse> clientes = clienteService.listarTodos();

        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ClienteDetalhesResponse> buscarPorId(@PathVariable Long id) {
        ClienteDetalhesResponse cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/status")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ClienteListarResponse>> listarPorStatus(@RequestParam String status) {
        List<ClienteListarResponse> clientes = clienteService.listarPorStatus(status);

        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/pesquisar")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ClienteListarResponse>> pesquisar(@RequestParam String pesquisa) {
        List<ClienteListarResponse> clientes = clienteService.pesquisarPorNomeOuCpfCnpj(pesquisa);

        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }
}

