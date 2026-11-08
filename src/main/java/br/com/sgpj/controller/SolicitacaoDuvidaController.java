package br.com.sgpj.controller;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoCandidatoDuplicidadeResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.request.SolicitacaoAceiteRequest;
import jakarta.validation.Valid;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.security.IpAddressUtil;
import br.com.sgpj.security.RateLimiter;
import br.com.sgpj.service.CloudflareTurnstileService;
import br.com.sgpj.service.SolicitacaoDuvidaService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import br.com.sgpj.dto.response.Processos.SincronizacaoProcessoResponse;
import br.com.sgpj.service.SincronizacaoStatusService;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;
import java.util.Locale;
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
    private final CloudflareTurnstileService cloudflareTurnstileService;
    private final AuthorizationUtil authorizationUtil;
    private final SincronizacaoStatusService sincronizacaoStatusService;

    public SolicitacaoDuvidaController(
            SolicitacaoDuvidaService solicitacaoDuvidaService,
            RateLimiter rateLimiter,
            CloudflareTurnstileService cloudflareTurnstileService,
            AuthorizationUtil authorizationUtil,
            SincronizacaoStatusService sincronizacaoStatusService
    ) {
        this.solicitacaoDuvidaService = solicitacaoDuvidaService;
        this.rateLimiter = rateLimiter;
        this.cloudflareTurnstileService = cloudflareTurnstileService;
        this.authorizationUtil = authorizationUtil;
        this.sincronizacaoStatusService = sincronizacaoStatusService;
    }

    @PostMapping
    @Operation(summary = "Recebe uma dúvida enviada pelo formulário público")
    public ResponseEntity<Void> criar(@RequestBody @Valid SolicitacaoDuvidaCriacaoRequest request,
                                                                     HttpServletRequest httpRequest) {
        String identifier = IpAddressUtil.getClientIpAddress(httpRequest);
        rateLimiter.checkFormRateLimit(identifier);
        cloudflareTurnstileService.validarToken(request.turnstileToken(), identifier);

        SolicitacaoDuvidaDetalhesResponse solicitacao = solicitacaoDuvidaService.criar(request);
        URI location = URI.create("/solicitacoes/" + solicitacao.id());
        return ResponseEntity.created(location).build();
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
            description = "Lista somente solicitações pendentes, filtrando por estado, tipo (PF/PJ), nome, e-mail, número CNJ " +
                    "e período de envio. A data final é inclusiva. O tamanho padrão é 20, com máximo de 100 registros por página. " +
                    "A ordenação aceita id, dataHoraInicio, ultimaAtualizacao e descricaoCaso; o ID é usado " +
                    "como desempate estável.")
    @ApiResponse(responseCode = "200", description = "Página de solicitações filtradas")
    @ApiResponse(responseCode = "400", description = "Período, paginação ou ordenação inválidos")
    public ResponseEntity<Page<SolicitacaoDuvidaListarResponse>> filtrar(
            @Parameter(description = "UF exata informada no formulário", example = "SP")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Tipo de solicitante: PF ou PJ", example = "PF")
            @RequestParam(required = false) String tipo,
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
                estado, parseTipo(tipo), pesquisa, inicio, fimExclusivo, pageable));
    }

    private Boolean parseTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return null;
        }
        return switch (tipo.trim().toUpperCase(Locale.ROOT)) {
            case "PF" -> true;
            case "PJ" -> false;
            default -> throw new IllegalArgumentException("tipo deve ser PF ou PJ");
        };
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

    @GetMapping("/{id}/duplicidades")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Busca possíveis clientes duplicados para uma solicitação")
    public ResponseEntity<List<SolicitacaoCandidatoDuplicidadeResponse>> buscarDuplicidades(@PathVariable Long id) {
        return ResponseEntity.ok(solicitacaoDuvidaService.buscarDuplicidades(id));
    }

    @PatchMapping("/{id}/aceitar")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('ACEITAR SOLICITAÇÃO') || hasAuthority('TODAS AS PERMISSÕES')")
        @Operation(
            summary = "Aceita a solicitação preenchendo dados ausentes de cliente e processo",
            description = "Recebe os dados validados do modal. Cliente e processo são sempre os vinculados ao ID da solicitação na URL."
        )
    public ResponseEntity<SolicitacaoClientePreenchimentoResponse> aceitar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitacaoAceiteRequest request) {
        Usuario usuarioAutenticado = authorizationUtil.getUsuarioLogado();
        return ResponseEntity.ok(solicitacaoDuvidaService.aceitar(id, request, usuarioAutenticado));
    }

    @PatchMapping("/{id}/negar")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('NEGAR SOLICITAÇÃO') || hasAuthority('TODAS AS PERMISSÕES')")
    @Operation(summary = "Nega a solicitação, removendo-a da lista de pendentes")
    public ResponseEntity<Void> negar(@PathVariable Long id) {
        solicitacaoDuvidaService.negar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/atualizar-dados")
    @PreAuthorize("hasAuthority('ACEITAR SOLICITAÇÃO') || hasAuthority('NEGAR SOLICITAÇÃO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Solicita a sincronização dos dados oficiais do processo da solicitação no DataJud")
    public ResponseEntity<SincronizacaoProcessoResponse> atualizarDadosOficiais(@PathVariable Long id) {
        SincronizacaoProcessoResponse response = solicitacaoDuvidaService.solicitarSincronizacaoDataJud(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping(value = "/{id}/sincronizacao-status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Acompanha o status de sincronização da solicitação em tempo real via SSE")
    public SseEmitter statusSincronizacaoEmTempoReal(@PathVariable Long id) {
        return sincronizacaoStatusService.criarConexao(id);
    }
}
