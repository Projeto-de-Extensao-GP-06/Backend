package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.Cliente.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.Cliente.ClienteListarResponse;
import br.com.sgpj.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('CRIAR CLIENTE') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Cria um novo cliente e sua pasta no OneDrive")
    public ResponseEntity<ClienteDetalhesResponse> criar(@RequestBody @Valid ClienteCriacaoRequest request) {
        ClienteDetalhesResponse cliente = clienteService.criar(request);
        URI location = URI.create("/clientes/" + cliente.id());
        return ResponseEntity.created(location).body(cliente);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR CLIENTE') || hasAuthority('TODAS AS PERMISSÕES')")
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
    @Operation(summary = "Lista clientes cadastrados. Permite filtrar para excluir clientes no status LEAD via query param excluirLead=true")
    public ResponseEntity<List<ClienteListarResponse>> listarTodos(
            @RequestParam(required = false, defaultValue = "false") boolean excluirLead) {
        List<ClienteListarResponse> clientes = clienteService.listarTodos(excluirLead);

        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    public ResponseEntity<List<ClienteListarResponse>> listarTodos() {
        return listarTodos(false);
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

    @GetMapping("/filtrar")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Busca clientes por termo (nome, e-mail ou CPF/CNPJ), status e tipo de pessoa (PF/PJ), com paginação")
    public ResponseEntity<Page<ClienteListarResponse>> pesquisarComFiltros(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false, defaultValue = "Todos") String status,
            @RequestParam(required = false, defaultValue = "Todos") String tipoPessoa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nomeRazaoSocial,asc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1])
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));

        Page<ClienteListarResponse> clientes = clienteService.pesquisarComFiltros(termo, status, tipoPessoa, pageable);
        return ResponseEntity.ok(clientes);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EXCLUIR CLIENTE') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Exclui um cliente e sua pasta no OneDrive")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
