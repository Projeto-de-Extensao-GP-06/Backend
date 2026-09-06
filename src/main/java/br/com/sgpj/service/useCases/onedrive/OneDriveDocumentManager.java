package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.entity.documentos.TipoDocumento;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


public class OneDriveDocumentManager {

    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    private final UsuarioRepository usuarioRepository;
    private final DocumentoRepository documentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final CaminhoPadraoDeArmazenamentoRepository caminhoPadraoDeArmazenamentoRepository;
    private final ProcessoAppRepository processoAppRepository;
    private final ProcessoDocumentoRepository processoDocumentoRepository;

    public OneDriveDocumentManager(UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter,
                            UsuarioRepository usuarioRepository,
                            DocumentoRepository documentoRepository,
                            TipoDocumentoRepository tipoDocumentoRepository,
                            CaminhoPadraoDeArmazenamentoRepository caminhoPadraoDeArmazenamentoRepository,
                            ProcessoAppRepository processoAppRepository,
                            ProcessoDocumentoRepository processoDocumentoRepository) {
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
        this.usuarioRepository = usuarioRepository;
        this.documentoRepository = documentoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.caminhoPadraoDeArmazenamentoRepository = caminhoPadraoDeArmazenamentoRepository;
        this.processoAppRepository = processoAppRepository;
        this.processoDocumentoRepository = processoDocumentoRepository;
    }

    public Documento registrarDocumento(OneDriveItemResponse item, Long tipoDocumentoId, CaminhoPadraoDeArmazenamento caminhoDeArmazenamento, Long processoAppId) {
        Usuario usuario = obterUsuarioLogado();
        TipoDocumento tipo = tipoDocumentoRepository.findById(tipoDocumentoId)
                .orElseThrow(() -> new OneDriveErroException("Tipo de documento não encontrado com ID: " + tipoDocumentoId));

        Documento documento = documentoRepository.findByOnedriveItemId(item.id())
                .orElse(new Documento());

        boolean isNovo = documento.getId() == null;

        preencherDocumento(documento, item, caminhoDeArmazenamento, usuario, tipo, isNovo);
        Documento documentoSalvo = documentoRepository.save(documento);

        if (processoAppId != null) {
            vincularDocumentoAoProcesso(documentoSalvo, processoAppId, usuario);
        }

        return documentoSalvo;
    }

    public void inativarDocumentoPorArquivo(CaminhoPadraoDeArmazenamento caminhoArmazenamento, String nomeArquivo) {
        documentoRepository.findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue(
                nomeArquivo, caminhoArmazenamento.getId()
        ).ifPresent(doc -> {
            Usuario usuario = obterUsuarioLogado();
            doc.setAtivo(false);
            doc.setInativadoEm(LocalDateTime.now());
            doc.setInativadoPorUsuario(usuario);
            doc.setUltimaAtualizacao(LocalDateTime.now());
            documentoRepository.save(doc);
        });
    }

    public void inativarDocumentosDaPasta(CaminhoPadraoDeArmazenamento caminhoArmazenamento) {
        Usuario usuario = obterUsuarioLogado();
        List<Documento> documentos = documentoRepository.findByPadraoArmazenamentoIdAndAtivoTrue(caminhoArmazenamento.getId());

        for (Documento documento : documentos) {
            documento.setAtivo(false);
            documento.setInativadoEm(LocalDateTime.now());
            documento.setInativadoPorUsuario(usuario);
            documento.setUltimaAtualizacao(LocalDateTime.now());
            documento.setPadraoArmazenamento(null);
        }

        if (!documentos.isEmpty()) {
            documentoRepository.saveAll(documentos);
        }
    }

    public void desvincularDocumentosDaPasta(CaminhoPadraoDeArmazenamento caminhoArmazenamento) {
        List<Documento> documentos = documentoRepository.findByPadraoArmazenamentoId(caminhoArmazenamento.getId());

        for (Documento documento : documentos) {
            documento.setPadraoArmazenamento(null);
        }

        if (!documentos.isEmpty()) {
            documentoRepository.saveAll(documentos);
        }
    }

    public void atualizarDocumentoRenomeado(CaminhoPadraoDeArmazenamento caminhoArmazenamento, String nomeAtual, OneDriveItemResponse resposta) {
        documentoRepository.findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue(
                nomeAtual, caminhoArmazenamento.getId()
        ).ifPresent(doc -> {
            doc.setNomeOriginal(resposta.nome());
            doc.setUltimaAtualizacao(LocalDateTime.now());
            doc.setOnedriveUrlDownload(resposta.urlDownload());
            doc.setOnedriveUrlThumbnail(resposta.thumbnailUrl());
            documentoRepository.save(doc);
        });
    }

    public void removerVinculosDeDocumento(Long documentoId) {
        List<ProcessoDocumento> vinculos = processoDocumentoRepository.findByDocumentoId(documentoId);
        if (!vinculos.isEmpty()) {
            processoDocumentoRepository.deleteAll(vinculos);
        }
    }

    public void removerCaminho(CaminhoPadraoDeArmazenamento caminhoArmazenamento) {
        caminhoPadraoDeArmazenamentoRepository.delete(caminhoArmazenamento);
    }

    private void preencherDocumento(Documento documento,
                                    OneDriveItemResponse item,
                                    CaminhoPadraoDeArmazenamento caminhoDeArmazenamento,
                                    Usuario usuario,
                                    TipoDocumento tipo,
                                    boolean isNovo) {
        documento.setNomeOriginal(item.nome());
        documento.setMimeType(item.mimeType());
        documento.setTamanhoBytes(item.tamanhoBytes());
        documento.setOnedriveItemId(item.id());
        documento.setOnedriveUrlThumbnail(item.thumbnailUrl());
        documento.setOnedriveUrlDownload(item.urlDownload());
        documento.setUltimaAtualizacao(LocalDateTime.now());
        documento.setAtivo(true);
        documento.setPadraoArmazenamento(caminhoDeArmazenamento);
        documento.setCriadoPorUsuario(usuario);
        documento.setTipoDocumento(tipo);

        if (isNovo) {
            documento.setDataHoraCriacao(LocalDateTime.now());
        }
    }

    private void vincularDocumentoAoProcesso(Documento documentoSalvo, Long processoAppId, Usuario usuario) {
        ProcessoApp processo = processoAppRepository.findById(processoAppId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado: " + processoAppId));

        ProcessoDocumentoId pdId = new ProcessoDocumentoId(documentoSalvo.getId(), processoAppId);
        ProcessoDocumento pd = new ProcessoDocumento();
        pd.setId(pdId);
        pd.setDocumento(documentoSalvo);
        pd.setProcessoApp(processo);
        pd.setAnexadoPorUsuario(usuario);
        pd.setAnexadoEm(LocalDateTime.now());
        processoDocumentoRepository.save(pd);
    }

    private Usuario obterUsuarioLogado() {
        String email = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new OneDriveErroException("Usuário logado não encontrado: " + email);
        }
        return usuario;
    }
}
