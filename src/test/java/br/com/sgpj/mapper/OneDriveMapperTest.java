package br.com.sgpj.mapper;

import br.com.sgpj.dto.OneDriveItemResponse;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.ItemReference;
import com.microsoft.graph.models.Thumbnail;
import com.microsoft.graph.models.ThumbnailSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OneDriveMapper — Testes do mapeamento DriveItem → DTO")
class OneDriveMapperTest {

    @Test
    @DisplayName("toDto com item nulo deve retornar null")
    void toDtoComItemNulo() {
        assertNull(OneDriveMapper.toDto(null, null));
    }

    @Test
    @DisplayName("toDto deve mapear arquivo corretamente")
    void toDtoDeveMapearArquivo() {
        OffsetDateTime agora = OffsetDateTime.now();

        DriveItem item = new DriveItem();
        item.setId("id-abc");
        item.setName("relatorio.pdf");
        item.setSize(2048L);
        item.setCreatedDateTime(agora);
        item.setLastModifiedDateTime(agora);

        com.microsoft.graph.models.File file = new com.microsoft.graph.models.File();
        file.setMimeType("application/pdf");
        item.setFile(file);

        ItemReference parent = new ItemReference();
        parent.setPath("/drive/root:/sgpj/usuarios/joao");
        item.setParentReference(parent);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("@microsoft.graph.downloadUrl", "https://download.test/file");
        item.setAdditionalData(additionalData);

        OneDriveItemResponse dto = OneDriveMapper.toDto(item, "https://thumb.test");

        assertEquals("id-abc", dto.id());
        assertEquals("relatorio.pdf", dto.nome());
        assertEquals("arquivo", dto.tipo());
        assertEquals(2048L, dto.tamanhoBytes());
        assertEquals("application/pdf", dto.mimeType());
        assertEquals("https://download.test/file", dto.urlDownload());
        assertEquals("https://thumb.test", dto.thumbnailUrl());
        assertEquals(agora, dto.criadoEm());
        assertEquals("/drive/root:/sgpj/usuarios/joao/relatorio.pdf", dto.caminho());
    }

    @Test
    @DisplayName("toDto deve mapear pasta corretamente (tipo = pasta)")
    void toDtoDeveMapearPasta() {
        DriveItem item = new DriveItem();
        item.setId("id-pasta");
        item.setName("documentos");
        item.setFolder(new Folder());

        OneDriveItemResponse dto = OneDriveMapper.toDto(item, null);

        assertEquals("pasta", dto.tipo());
        assertNull(dto.mimeType());
        assertNull(dto.thumbnailUrl());
    }

    @Test
    @DisplayName("toDtoList com lista nula deve retornar lista vazia")
    void toDtoListComListaNula() {
        java.util.List<OneDriveItemResponse> resultado = OneDriveMapper.toDtoList(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("toDtoList deve mapear vários itens")
    void toDtoListDeveMapearVariosItens() {
        DriveItem item1 = new DriveItem();
        item1.setId("1");
        item1.setName("a.txt");

        DriveItem item2 = new DriveItem();
        item2.setId("2");
        item2.setName("b.txt");

        java.util.List<OneDriveItemResponse> resultado = OneDriveMapper.toDtoList(java.util.List.of(item1, item2));

        assertEquals(2, resultado.size());
        assertEquals("a.txt", resultado.get(0).nome());
        assertEquals("b.txt", resultado.get(1).nome());
    }

    @Test
    @DisplayName("toDtoList deve extrair thumbnail large quando disponível")
    void toDtoListDeveExtrairThumbnailLarge() {
        DriveItem item = new DriveItem();
        item.setId("1");
        item.setName("foto.jpg");

        Thumbnail large = new Thumbnail();
        large.setUrl("https://thumb.large");

        ThumbnailSet set = new ThumbnailSet();
        set.setLarge(large);
        item.setThumbnails(java.util.List.of(set));

        java.util.List<OneDriveItemResponse> resultado = OneDriveMapper.toDtoList(java.util.List.of(item));

        assertEquals("https://thumb.large", resultado.get(0).thumbnailUrl());
    }

    @Test
    @DisplayName("toDtoList deve usar medium quando large não existe")
    void toDtoListDeveUsarMediumComoFallback() {
        DriveItem item = new DriveItem();
        item.setId("1");
        item.setName("foto.jpg");

        Thumbnail medium = new Thumbnail();
        medium.setUrl("https://thumb.medium");

        ThumbnailSet set = new ThumbnailSet();
        set.setMedium(medium);
        item.setThumbnails(java.util.List.of(set));

        java.util.List<OneDriveItemResponse> resultado = OneDriveMapper.toDtoList(java.util.List.of(item));

        assertEquals("https://thumb.medium", resultado.get(0).thumbnailUrl());
    }

    @Test
    @DisplayName("toDtoList deve usar small como último fallback")
    void toDtoListDeveUsarSmallComoUltimoFallback() {
        DriveItem item = new DriveItem();
        item.setId("1");
        item.setName("foto.jpg");

        Thumbnail small = new Thumbnail();
        small.setUrl("https://thumb.small");

        ThumbnailSet set = new ThumbnailSet();
        set.setSmall(small);
        item.setThumbnails(java.util.List.of(set));

        java.util.List<OneDriveItemResponse> resultado = OneDriveMapper.toDtoList(java.util.List.of(item));

        assertEquals("https://thumb.small", resultado.get(0).thumbnailUrl());
    }
}
