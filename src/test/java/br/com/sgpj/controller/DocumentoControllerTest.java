package br.com.sgpj.controller;

import br.com.sgpj.dto.response.CaminhoClienteResponse;
import br.com.sgpj.dto.response.DocumentoResponse;
import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.service.DocumentoService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentoController — Testes dos endpoints REST")
class DocumentoControllerTest {

    @Mock private DocumentoService service;
    @Mock private HttpServletRequest httpRequest;
    @InjectMocks private DocumentoController controller;

    @BeforeEach
    void setup() {
        when(service.isCorrectScope(any(HttpServletRequest.class), eq("GENERAL_USE"))).thenReturn(true);
    }

    // ========== GET /tipos-documento ==========

    @Test
    @DisplayName("Listar tipos deve retornar 200 com lista")
    void listarTiposDeveRetornar200() {
        when(service.listarTiposDocumento()).thenReturn(List.of(
                new TipoDocumentoResponse(1L, "RG"),
                new TipoDocumentoResponse(2L, "CTPS")
        ));
        ResponseEntity<List<TipoDocumentoResponse>> resp = controller.listarTiposDocumento(httpRequest);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
    }

    @Test
    @DisplayName("Listar tipos deve retornar 204 quando vazio")
    void listarTiposDeveRetornar204() {
        when(service.listarTiposDocumento()).thenReturn(Collections.emptyList());
        assertEquals(HttpStatus.NO_CONTENT, controller.listarTiposDocumento(httpRequest).getStatusCode());
    }


    // ========== GET /cliente/{id}/caminhos ==========

    @Test
    @DisplayName("Listar caminhos do cliente deve retornar 200")
    void listarCaminhosClienteDeveRetornar200() {
        when(service.listarCaminhosDoCliente(1L)).thenReturn(List.of(
                new CaminhoClienteResponse(10L, "Rafael - 123", "rafael/1234"),
                new CaminhoClienteResponse(11L, "Rafael - 123", "rafael/5678")
        ));
        ResponseEntity<List<CaminhoClienteResponse>> resp = controller.listarCaminhosDoCliente(1L, httpRequest);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
        assertEquals(10L, resp.getBody().get(0).id());
    }

    @Test
    @DisplayName("Listar caminhos do cliente deve retornar 204 quando vazio")
    void listarCaminhosClienteDeveRetornar204() {
        when(service.listarCaminhosDoCliente(99L)).thenReturn(Collections.emptyList());
        assertEquals(HttpStatus.NO_CONTENT, controller.listarCaminhosDoCliente(99L, httpRequest).getStatusCode());
    }

    // ========== GET /por-caminho ==========

    @Test
    @DisplayName("Listar por caminho deve retornar 200")
    void listarPorCaminhoDeveRetornar200() {
        when(service.listarDocumentosPorCaminho(5L)).thenReturn(List.of(criarDocMock(1L, "contrato.pdf")));
        ResponseEntity<List<DocumentoResponse>> resp = controller.listarDocumentosPorCaminho(5L, httpRequest);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    @DisplayName("Listar por caminho deve retornar 204 quando vazio")
    void listarPorCaminhoDeveRetornar204() {
        when(service.listarDocumentosPorCaminho(99L)).thenReturn(Collections.emptyList());
        assertEquals(HttpStatus.NO_CONTENT, controller.listarDocumentosPorCaminho(99L, httpRequest).getStatusCode());
    }


    private DocumentoResponse criarDocMock(Long id, String nome) {
        return new DocumentoResponse(
                id, nome, "application/pdf", 1024L,
                "item-" + id, "https://download.url", "https://thumb.url",
                LocalDateTime.now(), LocalDateTime.now(), true,
                "RG", "rafael/1234", "João"
        );
    }
}

