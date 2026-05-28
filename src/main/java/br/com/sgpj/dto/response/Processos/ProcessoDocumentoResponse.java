package br.com.sgpj.dto.response.Processos;

import java.time.LocalDateTime;

public record ProcessoDocumentoResponse(
        Long documentoId,
        String nomeOriginal,
        String mimeType,
        String onedriveUrlDownload,
        Long anexadoPorUsuarioId,
        String anexadoPorUsuarioNome,
        LocalDateTime anexadoEm,
        String tipoDocumentoNome,
        Long caminhoPadraoId,
        String caminhoPadraoBase

) {
}
