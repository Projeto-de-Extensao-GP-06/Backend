package br.com.sgpj.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UsuarioNaoEncontradoException — Testes de segurança")
class UsuarioNaoEncontradoExceptionSecurityTest {

    @Test
    @DisplayName("Deve não expor email na mensagem quando criado com email")
    void deveNaoExporEmailNaMensagemQuandoCriadoComEmail() {
        // Given
        String email = "usuario@teste.com";

        // When
        UsuarioNaoEncontradoException exception = new UsuarioNaoEncontradoException(email);

        // Then
        assertEquals("Usuário não encontrado", exception.getMessage());
        assertFalse(exception.getMessage().contains(email), 
            "Email não deve estar exposto na mensagem de erro");
        assertFalse(exception.getMessage().contains("@"), 
            "Email não deve estar exposto na mensagem de erro");
    }

    @Test
    @DisplayName("Deve não expor ID na mensagem quando criado com ID")
    void deveNaoExporIdNaMensagemQuandoCriadoComId() {
        // Given
        Long id = 123L;

        // When
        UsuarioNaoEncontradoException exception = new UsuarioNaoEncontradoException(id);

        // Then
        assertEquals("Usuário não encontrado", exception.getMessage());
        assertFalse(exception.getMessage().contains(id.toString()), 
            "ID não deve estar exposto na mensagem de erro");
    }

    @Test
    @DisplayName("Deve retornar mesma mensagem para email e ID - evita enumeração")
    void deveRetornarMesmaMensagemParaEmailEId() {
        // Given
        String email = "usuario@teste.com";
        Long id = 123L;

        // When
        UsuarioNaoEncontradoException exceptionEmail = new UsuarioNaoEncontradoException(email);
        UsuarioNaoEncontradoException exceptionId = new UsuarioNaoEncontradoException(id);

        // Then
        assertEquals(exceptionEmail.getMessage(), exceptionId.getMessage(),
            "Mensagens devem ser idênticas para evitar enumeração");
    }

    @Test
    @DisplayName("Deve não expor emails com diferentes formatos")
    void deveNaoExporEmailsComDiferentesFormatos() {
        // Given
        String[] emails = {
            "simple@test.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "admin@company.com.br"
        };

        // When/Then
        for (String email : emails) {
            UsuarioNaoEncontradoException exception = new UsuarioNaoEncontradoException(email);
            assertEquals("Usuário não encontrado", exception.getMessage());
            assertFalse(exception.getMessage().contains(email), 
                "Email " + email + " não deve estar exposto na mensagem de erro");
            assertFalse(exception.getMessage().contains("@"), 
                "Email " + email + " não deve estar exposto na mensagem de erro");
        }
    }

    @Test
    @DisplayName("Deve não expor IDs de diferentes tamanhos")
    void deveNaoExporIdsDeDiferentesTamanhos() {
        // Given
        Long[] ids = {1L, 999L, 123456789L, 0L};

        // When/Then
        for (Long id : ids) {
            UsuarioNaoEncontradoException exception = new UsuarioNaoEncontradoException(id);
            assertEquals("Usuário não encontrado", exception.getMessage());
            assertFalse(exception.getMessage().contains(id.toString()), 
                "ID " + id + " não deve estar exposto na mensagem de erro");
        }
    }
}
