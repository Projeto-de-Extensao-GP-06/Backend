package br.com.sgpj.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OneDriveErroException — Testes da exceção")
class OneDriveErroExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com a mensagem correta")
    void deveCriarComMensagem() {
        OneDriveErroException ex = new OneDriveErroException("Erro de teste");

        assertEquals("Erro de teste", ex.getMessage());
    }

    @Test
    @DisplayName("Deve ser uma RuntimeException")
    void deveSerRuntimeException() {
        OneDriveErroException ex = new OneDriveErroException("teste");

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("Deve ser possível lançar e capturar a exceção")
    void deveLancarECapturar() {
        assertThrows(OneDriveErroException.class, () -> {
            throw new OneDriveErroException("falha");
        });
    }
}

