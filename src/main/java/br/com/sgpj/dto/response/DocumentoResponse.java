package br.com.sgpj.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record DocumentoResponse(
        @Schema(description = "Id do documento", example = "1")
        Long id,

        @Schema(description = "Nome original do arquivo", example = "contrato-locacao.pdf")
        String nomeOriginal,

        @Schema(description = "Tipo MIME do arquivo", example = "application/pdf")
        String mimeType,

        @Schema(description = "Tamanho do arquivo em bytes", example = "204800")
        Long tamanhoBytes,

        @Schema(description = "ID do item no OneDrive", example = "01ABCDEFGH123456")
        String onedriveItemId,

        @Schema(description = "URL para download do arquivo no OneDrive", example = "https://onedrive.com/download/...")
        String onedriveUrlDownload,

        @Schema(description = "URL da miniatura do arquivo no OneDrive", example = "https://onedrive.com/thumbnail/...")
        String onedriveUrlThumbnail,

        @Schema(description = "Data e hora de criação do documento", example = "2026-05-16T10:30:00")
        LocalDateTime dataHoraCriacao,

        @Schema(description = "Data e hora da última atualização", example = "2026-05-16T14:00:00")
        LocalDateTime ultimaAtualizacao,

        @Schema(description = "Indica se o documento está ativo", example = "true")
        Boolean ativo,

        @Schema(description = "Nome do tipo de documento", example = "Contrato")
        String tipoDocumentoNome,

        @Schema(description = "Nome do caminho de armazenamento", example = "rafael-silva-12345678901")
        String caminhoArmazenamentoNome,

        @Schema(description = "Nome do usuário que criou o documento", example = "Thauany Bezerra")
        String criadoPorUsuarioNome
) {}
