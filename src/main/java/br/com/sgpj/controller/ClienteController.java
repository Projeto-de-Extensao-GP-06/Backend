package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Cria um novo cliente e sua pasta no OneDrive")
    public ResponseEntity<ClienteDetalhesResponse> criar(@RequestBody @Valid ClienteCriacaoRequest request) {
        ClienteDetalhesResponse cliente = clienteService.criar(request);
        URI location = URI.create("/clientes/" + cliente.id());
        return ResponseEntity.created(location).body(cliente);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Atualiza um cliente existente. Se nome ou CPF/CNPJ mudar, renomeia a pasta no OneDrive")
    public ResponseEntity<ClienteDetalhesResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ClienteAtualizacaoRequest request) {
        ClienteDetalhesResponse cliente = clienteService.atualizar(id, request);
        return ResponseEntity.ok(cliente);
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

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Exclui um cliente e sua pasta no OneDrive")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

