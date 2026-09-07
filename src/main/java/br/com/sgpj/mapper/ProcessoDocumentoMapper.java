package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.Processos.ProcessoDocumentoResponse;
import br.com.sgpj.entity.Processo.ProcessoDocumento;

import java.util.List;

public final class ProcessoDocumentoMapper {

    private ProcessoDocumentoMapper() {}

    public static ProcessoDocumentoResponse toResponse(ProcessoDocumento processoDocumento) {
        if (processoDocumento == null || processoDocumento.getDocumento() == null) {
            return null;
        }

        return new ProcessoDocumentoResponse(
                processoDocumento.getDocumento().getId(),
                processoDocumento.getDocumento().getNomeOriginal(),
                processoDocumento.getDocumento().getMimeType(),
                processoDocumento.getDocumento().getOnedriveUrlDownload(),
                processoDocumento.getAnexadoPorUsuario() != null ? processoDocumento.getAnexadoPorUsuario().getId() : null,
                processoDocumento.getAnexadoPorUsuario() != null ? processoDocumento.getAnexadoPorUsuario().getNome() : null,
                processoDocumento.getAnexadoEm(),
                processoDocumento.getDocumento().getTipoDocumento() != null ? processoDocumento.getDocumento().getTipoDocumento().getNome() : null,
                processoDocumento.getDocumento().getPadraoArmazenamento() != null ? processoDocumento.getDocumento().getPadraoArmazenamento().getId() : null,
                processoDocumento.getDocumento().getPadraoArmazenamento() != null ? processoDocumento.getDocumento().getPadraoArmazenamento().getCaminho() : null
        );
    }

    public static List<ProcessoDocumentoResponse> toResponseList(List<ProcessoDocumento> processosDocumento) {
        return processosDocumento.stream()
                .map(ProcessoDocumentoMapper::toResponse)
                .toList();
    }
}
