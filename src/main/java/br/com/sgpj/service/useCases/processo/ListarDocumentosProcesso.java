package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.response.Processos.ProcessoDocumentoResponse;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.mapper.ProcessoDocumentoMapper;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.security.AuthorizationUtil;

import java.util.List;

public class ListarDocumentosProcesso {

    private final ProcessoDocumentoRepository processoDocumentoRepository;
    private final AuthorizationUtil authorizationUtil;

    public ListarDocumentosProcesso(ProcessoDocumentoRepository processoDocumentoRepository, AuthorizationUtil authorizationUtil) {
        this.processoDocumentoRepository = processoDocumentoRepository;
        this.authorizationUtil = authorizationUtil;
    }

    public List<ProcessoDocumentoResponse> listarDocumentosDoProcesso(Long processoAppId) {
        authorizationUtil.verificarAcessoProcesso(processoAppId);
        List<ProcessoDocumento> processosDocumento = processoDocumentoRepository.findByProcessoAppId(processoAppId);
        return ProcessoDocumentoMapper.toResponseList(processosDocumento);
    }
}
