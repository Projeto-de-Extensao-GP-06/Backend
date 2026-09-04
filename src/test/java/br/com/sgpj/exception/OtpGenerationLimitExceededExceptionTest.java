package br.com.sgpj.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OtpGenerationLimitExceededException - Testes")
class OtpGenerationLimitExceededExceptionTest {

    @Test
    @DisplayName("Deve criar exceção com mensagem")
    void deveCriarExcecaoComMensagem() {
        String mensagem = "Limite de geração de OTP excedido";
        OtpGenerationLimitExceededException exception = new OtpGenerationLimitExceededException(mensagem);

        assertEquals(mensagem, exception.getMessage());
    }

    @Test
    @DisplayName("Deve ser RuntimeException")
    void deveSerRuntimeException() {
        OtpGenerationLimitExceededException exception = new OtpGenerationLimitExceededException("Test");

        assertTrue(exception instanceof RuntimeException);
    }
}