package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.CaminhoArmazenamentoResponse;
import br.com.sgpj.dto.response.DocumentoResponse;
import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.entity.documentos.TipoDocumento;

import java.util.List;

public class DocumentoMapper {

    private DocumentoMapper() {}

    public static TipoDocumentoResponse toTipoDocumentoResponse(TipoDocumento tipo) {
        return new TipoDocumentoResponse(tipo.getId(), tipo.getNome());
    }

    public static List<TipoDocumentoResponse> toTipoDocumentoResponseList(List<TipoDocumento> tipos) {
        return tipos.stream().map(DocumentoMapper::toTipoDocumentoResponse).toList();
    }

    public static CaminhoArmazenamentoResponse toCaminhoResponse(CaminhoPadraoDeArmazenamento caminho) {
        return new CaminhoArmazenamentoResponse(caminho.getId(), caminho.getCaminho(), caminho.getCaminho());
    }

    public static List<CaminhoArmazenamentoResponse> toCaminhoResponseList(List<CaminhoPadraoDeArmazenamento> caminhos) {
        return caminhos.stream().map(DocumentoMapper::toCaminhoResponse).toList();
    }

    public static DocumentoResponse toDocumentoResponse(Documento doc) {
        return new DocumentoResponse(
                doc.getId(),
                doc.getNomeOriginal(),
                doc.getMimeType(),
                doc.getTamanhoBytes(),
                doc.getOnedriveItemId(),
                doc.getOnedriveUrlDownload(),
                doc.getOnedriveUrlThumbnail(),
                doc.getDataHoraCriacao(),
                doc.getUltimaAtualizacao(),
                doc.getAtivo(),
                doc.getTipoDocumento() != null ? doc.getTipoDocumento().getNome() : null,
                doc.getPadraoArmazenamento() != null ? doc.getPadraoArmazenamento().getCaminho() : null,
                doc.getCriadoPorUsuario() != null ? doc.getCriadoPorUsuario().getNome() : null
        );
    }

    public static List<DocumentoResponse> toDocumentoResponseList(List<Documento> documentos) {
        return documentos.stream().map(DocumentoMapper::toDocumentoResponse).toList();
    }
}

