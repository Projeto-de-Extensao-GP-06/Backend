package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.response.Processos.ProcessoDocumentoResponse;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.ProcessoDocumentoMapper;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import br.com.sgpj.service.RegistroHistoricoProcesso;

import java.time.LocalDateTime;

@Slf4j
public class AnexarDocumentoProcesso {

    private final ProcessoAppRepository processoAppRepository;
    private final DocumentoRepository documentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcessoDocumentoRepository processoDocumentoRepository;
    private final RegistroHistoricoProcesso registroHistorico;

    public AnexarDocumentoProcesso(ProcessoAppRepository processoAppRepository,
                                   DocumentoRepository documentoRepository,
                                   UsuarioRepository usuarioRepository,
                                   ProcessoDocumentoRepository processoDocumentoRepository,
                                   RegistroHistoricoProcesso registroHistorico) {
        this.processoAppRepository = processoAppRepository;
        this.documentoRepository = documentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.processoDocumentoRepository = processoDocumentoRepository;
        this.registroHistorico = registroHistorico;
    }

    public ProcessoDocumentoResponse anexarDocumento(Long processoId, Long documentoId, Long usuarioId) {
        ProcessoApp processo = processoAppRepository.findById(processoId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo não encontrado: " + processoId));

        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Documento não encontrado: " + documentoId));

        if (!usuarioRepository.existsById(usuarioId)) {
            log.warn("Usuário com ID {} não encontrado.", usuarioId);
            throw new UsuarioNaoEncontradoException(usuarioId);
        }
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        ProcessoDocumentoId processoDocumentoId = new ProcessoDocumentoId(documento.getId(), processo.getId());

        if (processoDocumentoRepository.existsById(processoDocumentoId)) {
            throw new ObjetoNaoEncontradoException("Este documento já está vinculado a este processo.");
        }

        ProcessoDocumento processoDocumento = new ProcessoDocumento();
        processoDocumento.setId(processoDocumentoId);
        processoDocumento.setDocumento(documento);
        processoDocumento.setProcessoApp(processo);
        processoDocumento.setAnexadoPorUsuario(usuario);
        processoDocumento.setAnexadoEm(LocalDateTime.now());

        ProcessoDocumento vinculoSalvo = processoDocumentoRepository.save(processoDocumento);
        registroHistorico.registrar(processo, usuario, RegistroHistoricoProcesso.DOCUMENTO,
                "Documento vinculado: " + documento.getNomeOriginal() + ".");
        return ProcessoDocumentoMapper.toResponse(vinculoSalvo);
    }
}
