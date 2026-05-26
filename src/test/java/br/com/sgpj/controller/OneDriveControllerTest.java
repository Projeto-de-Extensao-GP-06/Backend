package br.com.sgpj.controller;

import br.com.sgpj.dto.OneDriveItemResponse;
import br.com.sgpj.service.OneDriveService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveController — Testes dos endpoints REST")
class OneDriveControllerTest {

    @Mock private OneDriveService service;
    @Mock private HttpServletRequest httpRequest;
    @InjectMocks private OneDriveController controller;

    private OneDriveItemResponse dtoExemplo;

    @BeforeEach
    void setup() {
        dtoExemplo = new OneDriveItemResponse(
                "id-1", "teste.pdf", "arquivo", 1024L,
                "application/pdf", "https://download.url", "https://thumb.url",
                null, null, "/root:/sgpj/usuarios/rafael/1234/teste.pdf"
        );
        when(service.isCorrectScope(any(HttpServletRequest.class), eq("GENERAL_USE"))).thenReturn(true);
    }

    @Test
    @DisplayName("GET — Listar deve retornar 200 com lista")
    void listarDeveRetornar200() {
        when(service.listarArquivos(1L)).thenReturn(List.of(dtoExemplo));
        ResponseEntity<?> resposta = controller.listarArquivosDePasta(1L, httpRequest);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
    }

    @Test
    @DisplayName("GET — Listar deve retornar 204 quando vazio")
    void listarDeveRetornar204() {
        when(service.listarArquivos(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<?> resposta = controller.listarArquivosDePasta(1L, httpRequest);
        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
    }

    @Test
    @DisplayName("POST — Upload deve retornar 200")
    void uploadDeveRetornar200() throws IOException {
        when(service.uploadDocumento(eq("doc.pdf"), any(byte[].class), eq(1L), eq(1L)))
                .thenReturn(dtoExemplo);
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "doc.pdf", "application/pdf", "conteudo".getBytes());
        ResponseEntity<OneDriveItemResponse> resposta = controller.uploadArquivo(arquivo, 1L, 1L, httpRequest);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("id-1", resposta.getBody().id());
    }

    @Test
    @DisplayName("POST /pasta — CriarPasta deve retornar 200")
    void criarPastaDeveRetornar200() {
        OneDriveItemResponse pasta = new OneDriveItemResponse("id-pasta", "1234567", "pasta", null, null, null, null, null, null, null);
        when(service.criarPasta(1L, "1234567-89.2024.8.26.0100")).thenReturn(pasta);
        ResponseEntity<OneDriveItemResponse> resposta = controller.criarPasta(1L, "1234567-89.2024.8.26.0100", httpRequest);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("pasta", resposta.getBody().tipo());
    }

    @Test
    @DisplayName("PUT /arquivo/renomear — Renomear deve retornar 200")
    void renomearDeveRetornar200() {
        OneDriveItemResponse renomeado = new OneDriveItemResponse("id-1", "novo.pdf", "arquivo", 1024L, "application/pdf", null, null, null, null, null);
        when(service.renomearArquivo(1L, "antigo.pdf", "novo.pdf")).thenReturn(renomeado);
        ResponseEntity<OneDriveItemResponse> resposta = controller.renomearArquivo(1L, "antigo.pdf", "novo.pdf", httpRequest);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("novo.pdf", resposta.getBody().nome());
    }

    @Test
    @DisplayName("DELETE /arquivo — Excluir arquivo deve retornar 204")
    void excluirArquivoDeveRetornar204() {
        doNothing().when(service).excluirArquivo(1L, "doc.pdf");
        ResponseEntity<Void> resposta = controller.excluirArquivo(1L, "doc.pdf", httpRequest);
        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(service).excluirArquivo(1L, "doc.pdf");
    }

    @Test
    @DisplayName("DELETE /pasta — Excluir pasta deve retornar 204")
    void excluirPastaDeveRetornar204() {
        doNothing().when(service).excluirPasta(1L);
        ResponseEntity<Void> resposta = controller.excluirPasta(1L, httpRequest);
        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(service).excluirPasta(1L);
    }
}
