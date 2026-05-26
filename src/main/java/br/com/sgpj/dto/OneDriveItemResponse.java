package br.com.sgpj.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record OneDriveItemResponse(
        @Schema(description = "ID único do item no OneDrive", example = "01ABCDEFGH123456")
        String id,

        @Schema(description = "Nome do arquivo ou pasta", example = "contrato-locacao.pdf")
        String nome,

        @Schema(description = "Tipo do item: 'arquivo' ou 'pasta'", example = "arquivo")
        String tipo,

        @Schema(description = "Tamanho em bytes (null para pastas)", example = "204800")
        Long tamanhoBytes,

        @Schema(description = "Tipo MIME do arquivo", example = "application/pdf")
        String mimeType,

        @Schema(description = "Link temporário para download direto", example = "https://onedrive.com/download/...")
        String urlDownload,

        @Schema(description = "URL da miniatura (preview de imagens, PDFs etc.)", example = "https://onedrive.com/thumbnail/...")
        String thumbnailUrl,

        @Schema(description = "Data e hora de criação", example = "2026-05-16T10:30:00Z")
        OffsetDateTime criadoEm,

        @Schema(description = "Data e hora da última modificação", example = "2026-05-16T14:00:00Z")
        OffsetDateTime modificadoEm,

        @Schema(description = "Caminho completo no drive", example = "/root:/Documentos/arquivo.pdf")
        String caminho
) {}
