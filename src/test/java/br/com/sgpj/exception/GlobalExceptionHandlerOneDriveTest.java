package br.com.sgpj.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler — Testes do tratamento de exceções OneDrive")
class GlobalExceptionHandlerOneDriveTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar 500 para OneDriveErroException")
    void deveRetornar500ParaOneDriveErro() {
        OneDriveErroException ex = new OneDriveErroException("Erro no OneDrive");

        ResponseEntity<Map<String, Object>> resposta = handler.handleOneDriveError(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), resposta.getStatusCode().value());
        assertNotNull(resposta.getBody());
        assertEquals(500, resposta.getBody().get("status"));
        assertEquals("Erro no OneDrive", resposta.getBody().get("message"));
        assertNotNull(resposta.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Deve retornar 404 para UsuarioNaoEncontradoException")
    void deveRetornar404ParaUsuarioNaoEncontrado() {
        UsuarioNaoEncontradoException ex = new UsuarioNaoEncontradoException("admin@email.com");

        ResponseEntity<Map<String, Object>> resposta = handler.handleUsuarioNaoEncontrado(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), resposta.getStatusCode().value());
        assertNotNull(resposta.getBody().get("message"));
    }

    @Test
    @DisplayName("Deve retornar 409 para EmailDuplicadoException")
    void deveRetornar409ParaEmailDuplicado() {
        EmailDuplicadoException ex = new EmailDuplicadoException("admin@email.com");

        ResponseEntity<Map<String, Object>> resposta = handler.handleEmailDuplicado(ex);

        assertEquals(HttpStatus.CONFLICT.value(), resposta.getStatusCode().value());
        assertNotNull(resposta.getBody().get("message"));
    }
}

