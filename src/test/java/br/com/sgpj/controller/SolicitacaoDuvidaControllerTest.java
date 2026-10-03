package br.com.sgpj.controller;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.SolicitacaoStatus;
import br.com.sgpj.security.RateLimiter;
import br.com.sgpj.service.SolicitacaoDuvidaService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitacaoDuvidaController — Testes dos endpoints REST")
class SolicitacaoDuvidaControllerTest {

    @Mock private SolicitacaoDuvidaService service;
    @Mock private RateLimiter rateLimiter;
    @Mock private HttpServletRequest httpRequest;
    @InjectMocks private SolicitacaoDuvidaController controller;

    @Test
    @DisplayName("POST deve retornar 201 com location")
    void criarDeveRetornar201() {
        SolicitacaoDuvidaCriacaoRequest request = criarRequest();
        when(service.criar(request)).thenReturn(criarDetalhes());

        ResponseEntity<SolicitacaoDuvidaDetalhesResponse> response = controller.criar(request, httpRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/solicitacoes/30", response.getHeaders().getLocation().toString());
        assertEquals(30L, response.getBody().id());
        
        verify(rateLimiter).checkFormRateLimit(anyString());
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
    @DisplayName("GET por id deve retornar detalhes")
    void buscarPorIdDeveRetornarDetalhes() {
        when(service.buscarPorId(30L)).thenReturn(criarDetalhes());

        ResponseEntity<SolicitacaoDuvidaDetalhesResponse> response = controller.buscarPorId(30L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Maria Silva", response.getBody().nomeCompleto());
    }

    @Test
    @DisplayName("PATCH aceitar deve retornar dados para preenchimento de cliente")
    void aceitarDeveRetornarPreenchimentoCliente() {
        when(service.aceitar(30L)).thenReturn(new SolicitacaoClientePreenchimentoResponse(
                30L,
                10L,
                "Maria Silva",
                "Maria Silva",
                "maria@email.com",
                "11999999999",
                "SP",
                "10108901220228260292",
                "Preciso de ajuda com meu processo",
                "Instagram",
                null
        ));

        ResponseEntity<SolicitacaoClientePreenchimentoResponse> response = controller.aceitar(30L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().clienteId());
        assertEquals("Maria Silva", response.getBody().nomeRazaoSocial());
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
                "SP",
                "10108901220228260292",
                "Preciso de ajuda com meu processo",
                "Instagram",
                true
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
                "Instagram",
                true,
                LocalDateTime.of(2026, 5, 30, 10, 0),
                SolicitacaoStatus.PENDENTE,
                null,
                null
        );
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
                "Instagram",
                LocalDateTime.of(2026, 5, 30, 10, 0),
                SolicitacaoStatus.PENDENTE
        );
    }
}
