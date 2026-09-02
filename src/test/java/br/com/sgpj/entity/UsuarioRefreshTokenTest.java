package br.com.sgpj.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UsuarioRefreshToken — Testes da entidade")
class UsuarioRefreshTokenTest {

    @Test
    @DisplayName("Construtor vazio deve criar instância com campos nulos")
    void construtorVazio() {
        UsuarioRefreshToken token = new UsuarioRefreshToken();

        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getUsuario());
        assertNull(token.getExpiraEm());
        assertNull(token.getRevogado());
        assertNull(token.getCriadoEm());
    }

    @Test
    @DisplayName("Builder e getters/setters devem manipular os atributos corretamente")
    void builderESetters() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime expiracao = agora.plusDays(1);

        UsuarioRefreshToken token = UsuarioRefreshToken.builder()
                .id(1L)
                .token("token-123")
                .usuario(usuario)
                .criadoEm(agora)
                .expiraEm(expiracao)
                .revogado(false)
                .build();

        assertEquals(1L, token.getId());
        assertEquals("token-123", token.getToken());
        assertEquals(usuario, token.getUsuario());
        assertEquals(agora, token.getCriadoEm());
        assertEquals(expiracao, token.getExpiraEm());
        assertFalse(token.getRevogado());

        token.setRevogado(true);
        assertTrue(token.getRevogado());
    }
}
