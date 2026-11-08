package br.com.sgpj.controller;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.request.SolicitacaoAceiteRequest;
import br.com.sgpj.dto.response.Solicitacao.MotivoDuplicidadeEnum;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoCandidatoDuplicidadeResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.SolicitacaoStatus;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.security.RateLimiter;
import br.com.sgpj.service.CloudflareTurnstileService;
import br.com.sgpj.service.SolicitacaoDuvidaService;
import br.com.sgpj.dto.response.Processos.SincronizacaoProcessoResponse;
import br.com.sgpj.service.SincronizacaoStatusService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import br.com.sgpj.exception.CaptchaInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitacaoDuvidaController — Testes dos endpoints REST")
class SolicitacaoDuvidaControllerTest {

    @Mock private SolicitacaoDuvidaService service;
    @Mock private RateLimiter rateLimiter;
    @Mock private CloudflareTurnstileService cloudflareTurnstileService;
    @Mock private AuthorizationUtil authorizationUtil;
    @Mock private SincronizacaoStatusService sincronizacaoStatusService;
    @Mock private HttpServletRequest httpRequest;
    @InjectMocks private SolicitacaoDuvidaController controller;

    @Test
    @DisplayName("POST deve retornar 201 com location e validar captcha")
    void criarDeveRetornar201() {
        SolicitacaoDuvidaCriacaoRequest request = criarRequest();
        when(service.criar(request)).thenReturn(criarDetalhes());

        ResponseEntity<Void> response = controller.criar(request, httpRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/solicitacoes/30", response.getHeaders().getLocation().toString());
        assertNull(response.getBody());
        
        verify(rateLimiter).checkFormRateLimit(anyString());
        verify(cloudflareTurnstileService).validarToken(anyString(), anyString());
    }

    @Test
    @DisplayName("POST deve propagar exceção quando captcha for inválido")
    void criarDeveLancarExcecaoQuandoCaptchaInvalido() {
        SolicitacaoDuvidaCriacaoRequest request = criarRequest();
        doThrow(new CaptchaInvalidoException("Captcha inválido"))
                .when(cloudflareTurnstileService).validarToken(anyString(), anyString());

        assertThrows(CaptchaInvalidoException.class, () -> controller.criar(request, httpRequest));
    }

    @Test
    @DisplayName("GET deve retornar 200 com solicitações pendentes")
    void listarPendentesDeveRetornar200() {
        Pageable pageable = PageRequest.of(0, 20,
                Sort.by(Sort.Order.desc("dataHoraInicio"), Sort.Order.desc("id")));
        Page<SolicitacaoDuvidaListarResponse> pagina = new PageImpl<>(List.of(criarListagem()), pageable, 21);
        when(service.listarPendentes(pageable)).thenReturn(pagina);

        ResponseEntity<Page<SolicitacaoDuvidaListarResponse>> response = controller.listarPendentes(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Maria Silva", response.getBody().getContent().getFirst().nomeCompleto());
        assertEquals(0, response.getBody().getNumber());
        assertEquals(20, response.getBody().getSize());
        assertEquals(21, response.getBody().getTotalElements());
        assertEquals(2, response.getBody().getTotalPages());
        verify(service).listarPendentes(pageable);
    }

    @Test
    @DisplayName("GET deve retornar 200 com página vazia e metadados")
    void listarPendentesDeveRetornar200ComPaginaVazia() {
        Pageable pageable = PageRequest.of(2, 5);
        Page<SolicitacaoDuvidaListarResponse> paginaVazia = new PageImpl<>(List.of(), pageable, 0);
        when(service.listarPendentes(pageable)).thenReturn(paginaVazia);

        ResponseEntity<Page<SolicitacaoDuvidaListarResponse>> response = controller.listarPendentes(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getContent().size());
        assertEquals(2, response.getBody().getNumber());
        assertEquals(5, response.getBody().getSize());
        assertEquals(0, response.getBody().getTotalElements());
        assertEquals(0, response.getBody().getTotalPages());
        verify(service).listarPendentes(pageable);
    }

    @Test
    @DisplayName("GET filtrar deve validar e delegar filtros, paginação e ordenação estável")
    void filtrarDeveDelegarFiltrosEPaginacao() {
        Pageable pageable = PageRequest.of(1, 5,
                Sort.by(Sort.Order.desc("dataHoraInicio"), Sort.Order.desc("id")));
        Page<SolicitacaoDuvidaListarResponse> pagina = new PageImpl<>(List.of(), pageable, 7);
        when(service.filtrar("SP", true, "maria",
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2027, 1, 1, 0, 0), pageable))
                .thenReturn(pagina);

        ResponseEntity<Page<SolicitacaoDuvidaListarResponse>> response = controller.filtrar(
                "SP", "PF", "maria", java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 12, 31), 1, 5, "dataSolicitacao,desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7, response.getBody().getTotalElements());
        verify(service).filtrar("SP", true, "maria",
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2027, 1, 1, 0, 0), pageable);
    }

    @Test
    @DisplayName("GET filtrar deve aceitar filtros ausentes e page vazia")
    void filtrarDeveAceitarFiltrosAusentes() {
        Pageable pageable = PageRequest.of(0, 20,
                Sort.by(Sort.Order.desc("dataHoraInicio"), Sort.Order.desc("id")));
        Page<SolicitacaoDuvidaListarResponse> paginaVazia = new PageImpl<>(List.of(), pageable, 0);
        when(service.filtrar(" ", null, " ", null, null, pageable)).thenReturn(paginaVazia);

        ResponseEntity<Page<SolicitacaoDuvidaListarResponse>> response = controller.filtrar(
                " ", null, " ", null, null, 0, 20, "dataHoraInicio,desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getTotalElements());
        assertEquals(0, response.getBody().getTotalPages());
        verify(service).filtrar(" ", null, " ", null, null, pageable);
    }

    @Test
    @DisplayName("GET filtrar deve rejeitar paginação fora dos limites")
    void filtrarDeveRejeitarParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> controller.filtrar(
                null, null, null, null, null, 0, 101, "id,asc"));
        assertThrows(IllegalArgumentException.class, () -> controller.filtrar(
                null, null, null, null, null, -1, 20, "id,asc"));
        assertThrows(IllegalArgumentException.class, () -> controller.filtrar(
                null, null, null, null, null, 0, 20, "nomeCompleto,asc"));
        assertThrows(IllegalArgumentException.class, () -> controller.filtrar(
                null, "Outro", null, null, null, 0, 20, "id,asc"));
    }

    @Test
    @DisplayName("GET por id deve retornar detalhes")
    void buscarPorIdDeveRetornarDetalhes() {
        when(service.buscarPorId(30L)).thenReturn(criarDetalhes());

        ResponseEntity<SolicitacaoDuvidaDetalhesResponse> response = controller.buscarPorId(30L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Maria Silva", response.getBody().nomeCompleto());
    }

    @Test
    @DisplayName("GET duplicidades deve retornar lista de candidatos")
    void buscarDuplicidadesDeveRetornarCandidatos() {
        SolicitacaoCandidatoDuplicidadeResponse candidato = new SolicitacaoCandidatoDuplicidadeResponse(
                50L,
                "Cliente Candidato",
                true,
                "ATIVO",
                LocalDateTime.now(),
                List.of(MotivoDuplicidadeEnum.EMAIL),
                List.of("maria@email.com")
        );
        when(service.buscarDuplicidades(30L)).thenReturn(List.of(candidato));

        ResponseEntity<List<SolicitacaoCandidatoDuplicidadeResponse>> response = controller.buscarDuplicidades(30L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(50L, response.getBody().getFirst().clienteId());
    }

    @Test
    @DisplayName("PATCH aceitar deve retornar dados para preenchimento de cliente")
    void aceitarDeveRetornarPreenchimentoCliente() {
        SolicitacaoAceiteRequest request = new SolicitacaoAceiteRequest(null, null);
                Usuario usuario = new Usuario();
                when(authorizationUtil.getUsuarioLogado()).thenReturn(usuario);
                when(service.aceitar(30L, request, usuario)).thenReturn(new SolicitacaoClientePreenchimentoResponse(
                30L,
                10L,
                "Maria Silva",
                "Maria Silva",
                null,
                "maria@email.com",
                "11999999999",
                "SP",
                "10108901220228260292",
                "Preciso de ajuda com meu processo",
                ComoConheceuEnum.INSTAGRAM,
                null,
                30L,
                "ATIVO",
                "ATIVO"
        ));

        ResponseEntity<SolicitacaoClientePreenchimentoResponse> response = controller.aceitar(30L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().clienteId());
        assertEquals("Maria Silva", response.getBody().nomeRazaoSocial());
        assertEquals("ATIVO", response.getBody().statusProcesso());
        verify(service).aceitar(30L, request, usuario);
    }

    @Test
    @DisplayName("PATCH negar deve retornar 204")
    void negarDeveRetornar204() {
        doNothing().when(service).negar(30L);

        ResponseEntity<Void> response = controller.negar(30L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).negar(30L);
    }

    private SolicitacaoDuvidaCriacaoRequest criarRequest() {
        return new SolicitacaoDuvidaCriacaoRequest(
                "Maria Silva",
                "maria@email.com",
                true,
                "11999999999",
                "10108901220228260292",
                "Preciso de ajuda com meu processo",
                ComoConheceuEnum.INSTAGRAM,
                true,
                null,
                null
        );
    }

    private SolicitacaoDuvidaDetalhesResponse criarDetalhes() {
        return new SolicitacaoDuvidaDetalhesResponse(
                30L,
                10L,
                "Maria Silva",
                "maria@email.com",
                "11999999999",
                "SP",
                "10108901220228260292",
                "Preciso de ajuda com meu processo",
                ComoConheceuEnum.INSTAGRAM,
                true,
                LocalDateTime.of(2026, 5, 30, 10, 0),
                SolicitacaoStatus.PENDENTE,
                null,
                null,
                true,
                null,
                null,
                null,
                LocalDateTime.of(2026, 5, 30, 10, 0)
        );
    }

    @Test
    @DisplayName("POST /solicitacoes/{id}/atualizar-dados deve retornar 202 com resposta de sincronização")
    void atualizarDadosOficiaisDeveRetornar202() {
        SincronizacaoProcessoResponse resposta = new SincronizacaoProcessoResponse(
                30L, "Solicitação de sincronização enviada para a fila de processamento.", "EM_PROCESSAMENTO");
        when(service.solicitarSincronizacaoDataJud(30L)).thenReturn(resposta);

        ResponseEntity<SincronizacaoProcessoResponse> response = controller.atualizarDadosOficiais(30L);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(30L, response.getBody().processoId());
        verify(service).solicitarSincronizacaoDataJud(30L);
    }

    @Test
    @DisplayName("GET /solicitacoes/{id}/sincronizacao-status deve retornar SseEmitter")
    void statusSincronizacaoEmTempoRealDeveRetornarEmitter() {
        SseEmitter emitter = new SseEmitter();
        when(sincronizacaoStatusService.criarConexao(30L)).thenReturn(emitter);

        SseEmitter result = controller.statusSincronizacaoEmTempoReal(30L);

        assertNotNull(result);
        verify(sincronizacaoStatusService).criarConexao(30L);
    }

    private SolicitacaoDuvidaListarResponse criarListagem() {
        return new SolicitacaoDuvidaListarResponse(
                30L,
                10L,
                "Maria Silva",
                "maria@email.com",
                "11999999999",
                "SP",
                "10108901220228260292",
                "Preciso de ajuda com meu processo",
                ComoConheceuEnum.INSTAGRAM,
                LocalDateTime.of(2026, 5, 30, 10, 0),
                SolicitacaoStatus.PENDENTE,
                true,
                null,
                null,
                null,
                LocalDateTime.of(2026, 5, 30, 10, 0)
        );
    }
}
