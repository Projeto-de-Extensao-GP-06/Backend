package br.com.sgpj.controller;

import br.com.sgpj.dto.OneDriveItemResponse;
import br.com.sgpj.service.OneDriveService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveController — Testes dos endpoints REST")
class OneDriveControllerTest {

    @Mock
    private OneDriveService service;

    @InjectMocks
    private OneDriveController controller;

    private OneDriveItemResponse dtoExemplo;

    @BeforeEach
    void setup() {
        dtoExemplo = new OneDriveItemResponse(
                "id-1", "teste.pdf", "arquivo", 1024L,
                "application/pdf", "https://download.url", "https://thumb.url",
                null, null, "/root:/sgpj/usuarios/joao/teste.pdf"
        );
    }

    // ===================== GET /{nomePasta} =====================

    @Test
    @DisplayName("Listar deve retornar 200 com lista de arquivos")
    void listarDeveRetornar200() {
        when(service.listarArquivos("joao")).thenReturn(java.util.List.of(dtoExemplo));

        ResponseEntity<?> resposta = controller.listarArquivosDePasta("joao");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
    }

    @Test
    @DisplayName("Listar deve retornar 204 quando pasta vazia")
    void listarDeveRetornar204QuandoVazio() {
        when(service.listarArquivos("vazia")).thenReturn(Collections.emptyList());

        ResponseEntity<?> resposta = controller.listarArquivosDePasta("vazia");

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
    }

    // ===================== POST (upload) =====================

    @Test
    @DisplayName("Upload deve retornar 200 com dados do arquivo")
    void uploadDeveRetornar200() throws IOException {
        when(service.uploadDocumento(eq("joao"), eq("doc.pdf"), any(byte[].class)))
                .thenReturn(dtoExemplo);

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "doc.pdf", "application/pdf", "conteudo".getBytes()
        );

        ResponseEntity<OneDriveItemResponse> resposta = controller.uploadArquivo("joao", arquivo);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("id-1", resposta.getBody().id());
    }

    // ===================== POST /{nomePasta} (criar pasta) =====================

    @Test
    @DisplayName("CriarPasta deve retornar 200 com dados da pasta")
    void criarPastaDeveRetornar200() {
        OneDriveItemResponse pasta = new OneDriveItemResponse(
                "id-pasta", "joao", "pasta", null,
                null, null, null, null, null, null
        );
        when(service.criarPasta("joao")).thenReturn(pasta);

        ResponseEntity<OneDriveItemResponse> resposta = controller.criarPasta("joao");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("joao", resposta.getBody().nome());
        assertEquals("pasta", resposta.getBody().tipo());
    }

    // ===================== PATCH /arquivo/renomear =====================

    @Test
    @DisplayName("Renomear deve retornar 200 com dados atualizados")
    void renomearDeveRetornar200() {
        OneDriveItemResponse renomeado = new OneDriveItemResponse(
                "id-1", "novo-nome.pdf", "arquivo", 1024L,
                "application/pdf", null, null, null, null, null
        );
        when(service.renomearArquivo("joao", "antigo.pdf", "novo-nome.pdf")).thenReturn(renomeado);

        ResponseEntity<OneDriveItemResponse> resposta = controller.renomearArquivo("joao", "antigo.pdf", "novo-nome.pdf");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("novo-nome.pdf", resposta.getBody().nome());
    }

    // ===================== DELETE /arquivo =====================

    @Test
    @DisplayName("Excluir arquivo deve retornar 204")
    void excluirArquivoDeveRetornar204() {
        doNothing().when(service).excluirArquivo("joao", "doc.pdf");

        ResponseEntity<Void> resposta = controller.excluirArquivo("joao", "doc.pdf");

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(service).excluirArquivo("joao", "doc.pdf");
    }

    // ===================== DELETE /{nomePasta} =====================

    @Test
    @DisplayName("Excluir pasta deve retornar 204")
    void excluirPastaDeveRetornar204() {
        doNothing().when(service).excluirPasta("joao");

        ResponseEntity<Void> resposta = controller.excluirPasta("joao");

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(service).excluirPasta("joao");
    }
}
