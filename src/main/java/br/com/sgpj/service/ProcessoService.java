package br.com.sgpj.service;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.*;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.repository.*;
import br.com.sgpj.service.useCases.processo.AnexarDocumentoProcesso;
import br.com.sgpj.service.useCases.processo.CreateProcesso;
import br.com.sgpj.service.useCases.processo.DeleteProcessoApp;
import br.com.sgpj.service.useCases.processo.DeleteProcessoDocumento;
import br.com.sgpj.service.useCases.processo.ListarDocumentosProcesso;
import br.com.sgpj.service.useCases.processo.ReadProcesso;
import br.com.sgpj.service.useCases.processo.UpdateProcesso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProcessoService {

    private final CreateProcesso createProcesso;
    private final UpdateProcesso updateProcesso;
    private final ReadProcesso readProcesso;
    private final AnexarDocumentoProcesso anexarDocumentoProcesso;
    private final ListarDocumentosProcesso listarDocumentosProcesso;
    private final DeleteProcessoApp deleteProcessoApp;
    private final DeleteProcessoDocumento deleteProcessoDocumento;

    public ProcessoService(CreateProcesso createProcesso,
                           UsuarioRepository usuarioRepository,
                           ProcessoAppRepository processoAppRepository,
                           UpdateProcesso updateProcesso,
                           ReadProcesso readProcesso,
                           DocumentoRepository documentoRepository,
                           ProcessoDocumentoRepository processoDocumentoRepository,
                           AuthorizationUtil authorizationUtil,
                           DeleteProcessoApp deleteProcessoApp,
                           DeleteProcessoDocumento deleteProcessoDocumento,
                           RegistroHistoricoProcesso registroHistorico) {
        this.createProcesso = createProcesso;
        this.updateProcesso = updateProcesso;
        this.readProcesso = readProcesso;
        this.deleteProcessoApp = deleteProcessoApp;
        this.deleteProcessoDocumento = deleteProcessoDocumento;
        this.anexarDocumentoProcesso = new AnexarDocumentoProcesso(processoAppRepository, documentoRepository, usuarioRepository, processoDocumentoRepository, registroHistorico);
        this.listarDocumentosProcesso = new ListarDocumentosProcesso(processoDocumentoRepository, authorizationUtil);
    }

    // CREATE OPERATIONS
    public ProcessoAppCreateResponse criarProcesso(ProcessoAppCriacaoRequest novoProcesso, Usuario usuarioAutenticado) {
        return createProcesso.criarProcesso(novoProcesso, usuarioAutenticado);
    }

    // READ OPERATIONS
    public Page<ProcessoAppMiniResponse> listarProcessos(
            Pageable pageable,
            String query,
            String status,
            Long faseId,
            Long tribunalId,
            Long prioridadeId,
            String audiencia
    ) {
        return readProcesso.listarProcessos(pageable, query, status, faseId, tribunalId, prioridadeId, audiencia);
    }

    public ProcessoAppResponse buscarPorId(Long id){
        return readProcesso.buscarPorId(id);
    }

    public List<ProcessoAppMiniResponse> buscarPorClienteId(Long clienteId) {
        return readProcesso.buscarPorClienteId(clienteId);
    }

    public List<ProcessoAppMiniResponse> buscaFiltro(String query, Long faseId, Long tribunalId) {
        return readProcesso.buscaFiltro(query, faseId, tribunalId);
    }

        // UPDATE OPERATIONS
    public ProcessoAppUpdateResponse atualizarProcesso(Long id, ProcessoAppUpdateRequest processoAtualizar){
        return updateProcesso.atualizarProcesso(id, processoAtualizar);
    }

    // DELETE OPERATIONS
    public void deletarProcesso(Long id){
        deleteProcessoApp.deletarProcesso(id);
    }


    // DOCUMENTS OPERATIONS
    public ProcessoDocumentoResponse anexarDocumento(Long processoId, Long documentoId, Long usuarioId) {
        return anexarDocumentoProcesso.anexarDocumento(processoId, documentoId, usuarioId);
    }

    public List<ProcessoDocumentoResponse> listarDocumentosDoProcesso(Long processoAppId) {
        return listarDocumentosProcesso.listarDocumentosDoProcesso(processoAppId);
    }

    public void desvincularDocumento(Long processoId, Long documentoId) {
        deleteProcessoDocumento.desvincularDocumento(processoId, documentoId);
    }

    public ProcessoAppResponse atualizarDadosDoDataJud(Long id) {
        return updateProcesso.atualizarDadosDoDataJud(id);
    }
}
