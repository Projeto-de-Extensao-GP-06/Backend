package br.com.sgpj.controller;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.SolicitacaoDuvidaService;
import br.com.sgpj.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/solicitacoes")
@Tag(name = "Solicitações")
public class SolicitacaoDuvidaController {

    private final SolicitacaoDuvidaService solicitacaoDuvidaService;
    private final UsuarioService usuarioService;

    public SolicitacaoDuvidaController(SolicitacaoDuvidaService solicitacaoDuvidaService,
                                       UsuarioService usuarioService) {
        this.solicitacaoDuvidaService = solicitacaoDuvidaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(summary = "Recebe uma dúvida enviada pelo formulário público")
    public ResponseEntity<SolicitacaoDuvidaDetalhesResponse> criar(@RequestBody @Valid SolicitacaoDuvidaCriacaoRequest request) {
        SolicitacaoDuvidaDetalhesResponse solicitacao = solicitacaoDuvidaService.criar(request);
        URI location = URI.create("/solicitacoes/" + solicitacao.id());
        return ResponseEntity.created(location).body(solicitacao);
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista solicitações pendentes")
    public ResponseEntity<List<SolicitacaoDuvidaListarResponse>> listarPendentes(HttpServletRequest httpRequest) {
        validarEscopo(httpRequest, "visualização de solicitações");

        List<SolicitacaoDuvidaListarResponse> solicitacoes = solicitacaoDuvidaService.listarPendentes();
        if (solicitacoes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(solicitacoes);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Busca os detalhes de uma solicitação")
    public ResponseEntity<SolicitacaoDuvidaDetalhesResponse> buscarPorId(@PathVariable Long id,
                                                                         HttpServletRequest httpRequest) {
        validarEscopo(httpRequest, "visualização de solicitação");

        return ResponseEntity.ok(solicitacaoDuvidaService.buscarPorId(id));
    }

    @PatchMapping("/{id}/aceitar")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('ACEITAR SOLICITAÇÃO') || hasAuthority('TODAS AS PERMISSÕES')")
    @Operation(summary = "Aceita a solicitação e retorna dados para preencher o cadastro de cliente")
    public ResponseEntity<SolicitacaoClientePreenchimentoResponse> aceitar(@PathVariable Long id,
                                                                           HttpServletRequest httpRequest) {
        validarEscopo(httpRequest, "aceite de solicitação");

        return ResponseEntity.ok(solicitacaoDuvidaService.aceitar(id));
    }

    @PatchMapping("/{id}/negar")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('NEGAR SOLICITAÇÃO') || hasAuthority('TODAS AS PERMISSÕES')")
    @Operation(summary = "Nega a solicitação, removendo-a da lista de pendentes")
    public ResponseEntity<Void> negar(@PathVariable Long id, HttpServletRequest httpRequest) {
        validarEscopo(httpRequest, "negação de solicitação");

        solicitacaoDuvidaService.negar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void validarEscopo(HttpServletRequest httpRequest, String acao) {
        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para " + acao + ".");
        }
    }
}
