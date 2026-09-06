package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.service.OneDriveService;
import br.com.sgpj.service.useCases.processo.DeleteProcessoDocumento;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DeletarDocumentosCliente {

    private final CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    private final DocumentoRepository documentoRepository;
    private final OneDriveService oneDriveService;
    private final ProcessoAppRepository processoAppRepository;
    private final ProcessoDocumentoRepository processoDocumentoRepository;
    private final DeleteProcessoDocumento deleteProcessoDocumento;

    public DeletarDocumentosCliente(CaminhoPadraoDeArmazenamentoRepository caminhoRepository,
                                     DocumentoRepository documentoRepository,
                                     OneDriveService oneDriveService,
                                     ProcessoAppRepository processoAppRepository,
                                     ProcessoDocumentoRepository processoDocumentoRepository,
                                     DeleteProcessoDocumento deleteProcessoDocumento) {
        this.caminhoRepository = caminhoRepository;
        this.documentoRepository = documentoRepository;
        this.oneDriveService = oneDriveService;
        this.processoAppRepository = processoAppRepository;
        this.processoDocumentoRepository = processoDocumentoRepository;
        this.deleteProcessoDocumento = deleteProcessoDocumento;
    }

    public void deletarDocumentosCliente(Long clienteId) {
        List<CaminhoPadraoDeArmazenamento> caminhos = new ArrayList<>(caminhoRepository.findByClienteId(clienteId));
        Set<Long> caminhoIds = new HashSet<>(caminhos.stream()
                .map(CaminhoPadraoDeArmazenamento::getId)
                .toList());
        List<Documento> documentos = new ArrayList<>();
        Set<Long> documentoIds = new HashSet<>();

        // Remove dependências dos processos do cliente
        List<ProcessoApp> processos = processoAppRepository.findByClienteId(clienteId);
        for (ProcessoApp processo : processos) {
            List<ProcessoDocumento> vinculosDocumentos = processoDocumentoRepository.findByProcessoAppId(processo.getId());
            for (ProcessoDocumento vinculoDocumento : vinculosDocumentos) {
                Documento documento = vinculoDocumento.getDocumento();
                if (documento != null && documento.getId() != null && documentoIds.add(documento.getId())) {
                    documentos.add(documento);
                }
                if (documento != null && documento.getPadraoArmazenamento() != null
                        && documento.getPadraoArmazenamento().getId() != null
                        && caminhoIds.add(documento.getPadraoArmazenamento().getId())) {
                    caminhos.add(documento.getPadraoArmazenamento());
                }
            }
            deleteProcessoDocumento.deletarVinculos(vinculosDocumentos);
        }

        for (CaminhoPadraoDeArmazenamento caminho : caminhos) {
            for (Documento documento : documentoRepository.findByPadraoArmazenamentoId(caminho.getId())) {
                if (documento.getId() != null && documentoIds.add(documento.getId())) {
                    documentos.add(documento);
                }
            }
        }

        // Remove vínculos e documentos associados aos caminhos do cliente
        for (Documento documento : documentos) {
            deleteProcessoDocumento.deletarVinculosPorDocumentoId(documento.getId());
        }
        if (!documentos.isEmpty()) {
            documentoRepository.deleteAll(documentos);
        }

        // Remove pastas/caminhos associados usando a regra atual da main.
        if (!caminhos.isEmpty()) {
            List<Long> idsCaminhosParaExcluir = new ArrayList<>(caminhos.stream()
                    .map(CaminhoPadraoDeArmazenamento::getId)
                    .toList());
            idsCaminhosParaExcluir.forEach(oneDriveService::excluirPasta);
        }
    }
}
