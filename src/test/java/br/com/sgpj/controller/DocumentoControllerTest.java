package br.com.sgpj.controller;

import br.com.sgpj.dto.response.CaminhoArmazenamentoResponse;
import br.com.sgpj.dto.response.CaminhoClienteResponse;
import br.com.sgpj.dto.response.DocumentoResponse;
import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.DocumentoService;
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
@DisplayName("DocumentoController — Testes dos endpoints REST")
class DocumentoControllerTest {

    @Mock private DocumentoService service;
    @InjectMocks private DocumentoController controller;

    // ========== GET /tipos-documento ==========

    @Test
    @DisplayName("Listar tipos deve retornar 200 com lista")
    void listarTiposDeveRetornar200() {
        when(service.listarTiposDocumento()).thenReturn(List.of(
                new TipoDocumentoResponse(1L, "RG"),
                new TipoDocumentoResponse(2L, "CTPS")
        ));
        ResponseEntity<List<TipoDocumentoResponse>> resp = controller.listarTiposDocumento();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
    }

    @Test
    @DisplayName("Listar tipos deve retornar 204 quando vazio")
    void listarTiposDeveRetornar204() {
        when(service.listarTiposDocumento()).thenReturn(Collections.emptyList());
        assertEquals(HttpStatus.NO_CONTENT, controller.listarTiposDocumento().getStatusCode());
    }


    // ========== GET /cliente/{id}/caminhos ==========

    @Test
    @DisplayName("Listar caminhos do cliente deve retornar 200")
    void listarCaminhosClienteDeveRetornar200() {
        when(service.listarCaminhosDoCliente(1L)).thenReturn(List.of(
                new CaminhoClienteResponse(10L, "Rafael - 123", "rafael/1234"),
                new CaminhoClienteResponse(11L, "Rafael - 123", "rafael/5678")
        ));
        ResponseEntity<List<CaminhoClienteResponse>> resp = controller.listarCaminhosDoCliente(1L);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
        assertEquals(10L, resp.getBody().getFirst().id());
    }

    @Test
    @DisplayName("Listar caminhos do cliente deve retornar 204 quando vazio")
    void listarCaminhosClienteDeveRetornar204() {
        when(service.listarCaminhosDoCliente(99L)).thenReturn(Collections.emptyList());
        assertEquals(HttpStatus.NO_CONTENT, controller.listarCaminhosDoCliente(99L).getStatusCode());
    }

    // ========== GET /por-caminho ==========

    @Test
    @DisplayName("Listar por caminho deve retornar 200")
    void listarPorCaminhoDeveRetornar200() {
        when(service.listarDocumentosPorCaminho(5L)).thenReturn(List.of(criarDocMock(1L, "contrato.pdf")));
        ResponseEntity<List<DocumentoResponse>> resp = controller.listarDocumentosPorCaminho(5L);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    @DisplayName("Listar por caminho deve retornar 204 quando vazio")
    void listarPorCaminhoDeveRetornar204() {
        when(service.listarDocumentosPorCaminho(99L)).thenReturn(Collections.emptyList());
        assertEquals(HttpStatus.NO_CONTENT, controller.listarDocumentosPorCaminho(99L).getStatusCode());
    }

    // ========== GET /caminhos-armazenamento ==========

    @Test
    @DisplayName("Listar caminhos de armazenamento deve retornar 200 com lista")
    void listarCaminhosArmazenamentoDeveRetornar200() {
        when(service.listarCaminhosArmazenamento()).thenReturn(List.of(
                new CaminhoArmazenamentoResponse(1L, "rafael-silva-12345678901/1234567", "rafael-silva-12345678901/1234567")
        ));
        ResponseEntity<List<CaminhoArmazenamentoResponse>> resp = controller.listarCaminhosArmazenamento();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    @DisplayName("Listar caminhos de armazenamento deve retornar 204 quando vazio")
    void listarCaminhosArmazenamentoDeveRetornar204() {
        when(service.listarCaminhosArmazenamento()).thenReturn(Collections.emptyList());
        assertEquals(HttpStatus.NO_CONTENT, controller.listarCaminhosArmazenamento().getStatusCode());
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

