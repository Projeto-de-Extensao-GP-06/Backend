package br.com.sgpj.controller;

import br.com.sgpj.dto.response.Processos.ProcessoDocumentoResponse;
import br.com.sgpj.service.ProcessoService;
import br.com.sgpj.service.UsuarioService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessosController — Endpoints de processo_documento")
class ProcessosControllerTest {

    @Mock private ProcessoService processoService;
    @Mock private UsuarioService usuarioService;
    @InjectMocks private ProcessosController controller;

    private ProcessoDocumentoResponse docResponse;

    @BeforeEach
    void setup() {
        docResponse = new ProcessoDocumentoResponse(
                20L, "contrato.pdf", "application/pdf",
                "https://download.url", 1L, "Usuario Responsavel",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                "RG", 5L, "rafael-silva-12345678901/001"
        );
    }

    // ========== POST /processos/{id}/documentos ==========

    @Test
    @DisplayName("POST /processos/{id}/documentos — Deve retornar 201 ao anexar documento")
    void anexarDocumentoDeveRetornar201() {
        when(processoService.anexarDocumento(10L, 20L, 1L)).thenReturn(docResponse);

        ResponseEntity<ProcessoDocumentoResponse> resp = controller.anexarDocumento(10L, 20L, 1L);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(20L, resp.getBody().documentoId());
        assertEquals("contrato.pdf", resp.getBody().nomeOriginal());
        verify(processoService).anexarDocumento(10L, 20L, 1L);
    }

    @Test
    @DisplayName("POST /processos/{id}/documentos — Deve repassar processoId do path ao service")
    void anexarDocumentoDeveRepassarProcessoId() {
        when(processoService.anexarDocumento(42L, 7L, 3L)).thenReturn(docResponse);

        ResponseEntity<ProcessoDocumentoResponse> resp = controller.anexarDocumento(42L, 7L, 3L);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        verify(processoService).anexarDocumento(42L, 7L, 3L);
    }

    // ========== GET /processos/{id}/documentos ==========

    @Test
    @DisplayName("GET /processos/{id}/documentos — Deve retornar 200 com lista de documentos")
    void listarDocumentosDeveRetornar200() {
        when(processoService.listarDocumentosDoProcesso(10L)).thenReturn(List.of(docResponse));

        ResponseEntity<List<ProcessoDocumentoResponse>> resp = controller.listarDocumentosDoProcesso(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().size());
        assertEquals(20L, resp.getBody().get(0).documentoId());
        assertEquals("contrato.pdf", resp.getBody().get(0).nomeOriginal());
    }

    @Test
    @DisplayName("GET /processos/{id}/documentos — Deve retornar 204 quando processo sem documentos")
    void listarDocumentosDeveRetornar204() {
        when(processoService.listarDocumentosDoProcesso(10L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProcessoDocumentoResponse>> resp = controller.listarDocumentosDoProcesso(10L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        assertNull(resp.getBody());
    }

    @Test
    @DisplayName("GET /processos/{id}/documentos — Deve retornar múltiplos documentos")
    void listarDocumentosDeveRetornarMultiplos() {
        ProcessoDocumentoResponse doc2 = new ProcessoDocumentoResponse(
                21L, "procuracao.pdf", "application/pdf",
                "https://download2.url", 2L, "Outro Usuario",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                "Procuração", 5L, "rafael-silva-12345678901/001"
        );
        when(processoService.listarDocumentosDoProcesso(10L)).thenReturn(List.of(docResponse, doc2));

        ResponseEntity<List<ProcessoDocumentoResponse>> resp = controller.listarDocumentosDoProcesso(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
        assertEquals(20L, resp.getBody().get(0).documentoId());
        assertEquals(21L, resp.getBody().get(1).documentoId());
    }
}

