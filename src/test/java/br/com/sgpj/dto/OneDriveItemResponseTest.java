package br.com.sgpj.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OneDriveItemResponse — Testes do DTO")
class OneDriveItemResponseTest {

    @Test
    @DisplayName("Record deve armazenar e retornar todos os campos corretamente")
    void recordDeveTerTodosOsCampos() {
        OffsetDateTime agora = OffsetDateTime.now();

        OneDriveItemResponse dto = new OneDriveItemResponse(
                "id-123",
                "documento.pdf",
                "arquivo",
                1024L,
                "application/pdf",
                "https://download.url",
                "https://thumbnail.url",
                agora,
                agora,
                "/root:/sgpj/usuarios/joao/documento.pdf"
        );

        assertEquals("id-123", dto.id());
        assertEquals("documento.pdf", dto.nome());
        assertEquals("arquivo", dto.tipo());
        assertEquals(1024L, dto.tamanhoBytes());
        assertEquals("application/pdf", dto.mimeType());
        assertEquals("https://download.url", dto.urlDownload());
        assertEquals("https://thumbnail.url", dto.thumbnailUrl());
        assertEquals(agora, dto.criadoEm());
        assertEquals(agora, dto.modificadoEm());
        assertEquals("/root:/sgpj/usuarios/joao/documento.pdf", dto.caminho());
    }

    @Test
    @DisplayName("Record deve aceitar campos nulos para pasta")
    void recordComCamposNulos() {
        OneDriveItemResponse dto = new OneDriveItemResponse(
                "id-456", "fotos", "pasta",
                null, null, null, null, null, null, null
        );

        assertEquals("id-456", dto.id());
        assertEquals("fotos", dto.nome());
        assertEquals("pasta", dto.tipo());
        assertNull(dto.tamanhoBytes());
        assertNull(dto.mimeType());
        assertNull(dto.urlDownload());
    }

    @Test
    @DisplayName("Dois records com mesmos valores devem ser iguais")
    void equalsDeveCompararPorValor() {
        OffsetDateTime agora = OffsetDateTime.now();

        OneDriveItemResponse dto1 = new OneDriveItemResponse(
                "id-1", "teste.txt", "arquivo", 100L,
                "text/plain", null, null, agora, agora, null
        );
        OneDriveItemResponse dto2 = new OneDriveItemResponse(
                "id-1", "teste.txt", "arquivo", 100L,
                "text/plain", null, null, agora, agora, null
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}

