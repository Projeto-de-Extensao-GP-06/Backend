package br.com.sgpj.controller;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.security.IpAddressUtil;
import br.com.sgpj.security.RateLimiter;
import br.com.sgpj.service.SolicitacaoDuvidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RestController
@RequestMapping("/solicitacoes")
@Tag(name = "Solicitações")
public class SolicitacaoDuvidaController {

    private final SolicitacaoDuvidaService solicitacaoDuvidaService;
    private final RateLimiter rateLimiter;

    public SolicitacaoDuvidaController(SolicitacaoDuvidaService solicitacaoDuvidaService, RateLimiter rateLimiter) { 
        this.solicitacaoDuvidaService = solicitacaoDuvidaService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    @Operation(summary = "Recebe uma dúvida enviada pelo formulário público")
    public ResponseEntity<SolicitacaoDuvidaDetalhesResponse> criar(@RequestBody @Valid SolicitacaoDuvidaCriacaoRequest request,
                                                                     HttpServletRequest httpRequest) {
        String identifier = IpAddressUtil.getClientIpAddress(httpRequest);
        rateLimiter.checkFormRateLimit(identifier);
        
        SolicitacaoDuvidaDetalhesResponse solicitacao = solicitacaoDuvidaService.criar(request);
        URI location = URI.create("/solicitacoes/" + solicitacao.id());
        return ResponseEntity.created(location).body(solicitacao);
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista solicitações pendentes")
    public ResponseEntity<Page<SolicitacaoDuvidaListarResponse>> listarPendentes(
            @PageableDefault(size = 20, sort = {"dataHoraInicio", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(solicitacaoDuvidaService.listarPendentes(pageable));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Busca os detalhes de uma solicitação")
    public ResponseEntity<SolicitacaoDuvidaDetalhesResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitacaoDuvidaService.buscarPorId(id));
    }

    @PatchMapping("/{id}/aceitar")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('ACEITAR SOLICITAÇÃO') || hasAuthority('TODAS AS PERMISSÕES')")
    @Operation(summary = "Aceita a solicitação e retorna dados para preencher o cadastro de cliente")
    public ResponseEntity<SolicitacaoClientePreenchimentoResponse> aceitar(@PathVariable Long id) {
        return ResponseEntity.ok(solicitacaoDuvidaService.aceitar(id));
    }

    @PatchMapping("/{id}/negar")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('NEGAR SOLICITAÇÃO') || hasAuthority('TODAS AS PERMISSÕES')")
    @Operation(summary = "Nega a solicitação, removendo-a da lista de pendentes")
    public ResponseEntity<Void> negar(@PathVariable Long id) {
        solicitacaoDuvidaService.negar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
