package br.com.sgpj.controller;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.security.IpAddressUtil;
import br.com.sgpj.security.RateLimiter;
import br.com.sgpj.service.SolicitacaoDuvidaService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/solicitacoes")
@Tag(name = "Solicitações")
public class SolicitacaoDuvidaController {

    private static final int TAMANHO_MAXIMO = 100;
    private static final String TAMANHO_PADRAO_PARAMETRO = "20";
    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of(
            "id", "dataSolicitacao", "dataHoraInicio", "ultimaAtualizacao", "descricaoCaso");

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
    @Operation(
            summary = "Lista solicitações pendentes",
            description = "Retorna as solicitações pendentes de forma paginada. " +
                    "A ordenação padrão é por data de início e ID, ambos em ordem decrescente."
    )
    @ApiResponse(responseCode = "200", description = "Página de solicitações pendentes")
    public ResponseEntity<Page<SolicitacaoDuvidaListarResponse>> listarPendentes(
            @ParameterObject
            @PageableDefault(size = 20, sort = {"dataHoraInicio", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(solicitacaoDuvidaService.listarPendentes(pageable));
    }

    @GetMapping("/filtrar")
    @SecurityRequirement(name = "Bearer")
    @Operation(
            summary = "Filtra solicitações com paginação",
            description = "Lista somente solicitações pendentes, filtrando por estado, nome, e-mail, número CNJ " +
                    "e período de envio. A data final é inclusiva. O tamanho padrão é 20, com máximo de 100 registros por página. " +
                    "A ordenação aceita id, dataHoraInicio, ultimaAtualizacao e descricaoCaso; o ID é usado " +
                    "como desempate estável.")
    @ApiResponse(responseCode = "200", description = "Página de solicitações filtradas")
    @ApiResponse(responseCode = "400", description = "Período, paginação ou ordenação inválidos")
    public ResponseEntity<Page<SolicitacaoDuvidaListarResponse>> filtrar(
            @Parameter(description = "UF exata informada no formulário", example = "SP")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Busca parcial sem diferenciar maiúsculas/minúsculas em nome, e-mail e CNJ")
            @RequestParam(required = false) String pesquisa,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = TAMANHO_PADRAO_PARAMETRO) int size,
            @RequestParam(defaultValue = "dataHoraInicio,desc") String sort) {
        if (page < 0) {
            throw new IllegalArgumentException("page deve ser maior ou igual a zero");
        }
        if (size < 1 || size > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("size deve estar entre 1 e " + TAMANHO_MAXIMO);
        }
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("dataInicio deve ser anterior ou igual a dataFim");
        }

        Sort sortParametro = parseSort(sort);
        Sort sortEstavel = sortParametro.getOrderFor("id") == null
                ? sortParametro.and(Sort.by(sortParametro.iterator().next().getDirection(), "id"))
                : sortParametro;
        Pageable pageable = PageRequest.of(page, size, sortEstavel);

        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fimExclusivo;
        try {
            fimExclusivo = dataFim != null ? dataFim.plusDays(1).atStartOfDay() : null;
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("dataFim está fora do intervalo permitido");
        }
        return ResponseEntity.ok(solicitacaoDuvidaService.filtrar(
                estado, pesquisa, inicio, fimExclusivo, pageable));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            throw new IllegalArgumentException("sort não pode estar em branco");
        }
        String[] parts = sort.split(",", -1);
        if (parts.length > 2 || parts[0].isBlank() || !CAMPOS_ORDENAVEIS.contains(parts[0])) {
            throw new IllegalArgumentException("Campo de ordenação inválido");
        }
        Sort.Direction direction;
        try {
            direction = parts.length == 2 ? Sort.Direction.fromString(parts[1]) : Sort.Direction.ASC;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Direção de ordenação inválida");
        }
        String campo = "dataSolicitacao".equals(parts[0]) ? "dataHoraInicio" : parts[0];
        return Sort.by(direction, campo);
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
