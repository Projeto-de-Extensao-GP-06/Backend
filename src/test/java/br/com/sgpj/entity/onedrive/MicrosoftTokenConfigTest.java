package br.com.sgpj.entity.onedrive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MicrosoftTokenConfig — Testes da entidade")
class MicrosoftTokenConfigTest {

    @Test
    @DisplayName("Construtor vazio deve criar instância com campos nulos")
    void construtorVazio() {
        MicrosoftTokenConfig config = new MicrosoftTokenConfig();

        assertNull(config.getId());
        assertNull(config.getRefreshToken());
        assertNull(config.getDataCriacao());
    }

    @Test
    @DisplayName("Construtor com argumentos deve preencher todos os campos")
    void construtorComArgumentos() {
        LocalDateTime agora = LocalDateTime.now();

        MicrosoftTokenConfig config = new MicrosoftTokenConfig(1L, "token123", agora);

        assertEquals(1L, config.getId());
        assertEquals("token123", config.getRefreshToken());
        assertEquals(agora, config.getDataCriacao());
    }

    @Test
    @DisplayName("Setters devem atualizar os campos corretamente")
    void setters() {
        MicrosoftTokenConfig config = new MicrosoftTokenConfig();
        LocalDateTime agora = LocalDateTime.now();

        config.setId(5L);
        config.setRefreshToken("novoToken");
        config.setDataCriacao(agora);

        assertEquals(5L, config.getId());
        assertEquals("novoToken", config.getRefreshToken());
        assertEquals(agora, config.getDataCriacao());
    }
}

