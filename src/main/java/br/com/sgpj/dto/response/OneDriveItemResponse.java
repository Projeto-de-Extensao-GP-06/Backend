package br.com.sgpj.dto.response;

import java.time.OffsetDateTime;

/**
 * DTO de resposta para itens do OneDrive.
 * Retorna apenas os campos relevantes em vez do JSON gigante do DriveItem.
 *
 * @param id              ID único do item no OneDrive
 * @param nome            nome do arquivo ou pasta
 * @param tipo            "arquivo" ou "pasta"
 * @param tamanhoBytes    tamanho em bytes (null para pastas)
 * @param mimeType        tipo MIME do arquivo (ex: "application/pdf")
 * @param urlDownload     link temporário para download direto
 * @param thumbnailUrl    URL da miniatura (preview de imagens, PDFs etc.)
 * @param criadoEm        data/hora de criação
 * @param modificadoEm    data/hora da última modificação
 * @param caminho         caminho completo no drive (ex: "/root:/Documentos/arquivo.pdf")
 */
public record OneDriveItemResponse(
        String id,
        String nome,
        String tipo,
        Long tamanhoBytes,
        String mimeType,
        String urlDownload,
        String thumbnailUrl,
        OffsetDateTime criadoEm,
        OffsetDateTime modificadoEm,
        String caminho
) {}
