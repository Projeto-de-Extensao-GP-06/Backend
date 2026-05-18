package br.com.sgpj.mapper;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.ProcessoAppResponse;
import br.com.sgpj.entity.Processo.ProcessoApp;

import java.time.LocalDateTime;

public class ProcessoAppMapper {

    private ProcessoAppMapper() {
    }

    public static ProcessoApp toCreateEntity(ProcessoAppCriacaoRequest processoAppCriacaoRequest) {

        ProcessoApp entidade = new ProcessoApp();
        entidade.setDescricaoCaso(processoAppCriacaoRequest.descricaoCaso());
        entidade.setUltimaAtualizacao(LocalDateTime.now());
        entidade.setDataHoraInicio(LocalDateTime.now());
        entidade.setAtivo(true);
        entidade.setDataAudiencia(processoAppCriacaoRequest.dataAudiencia());
        entidade.setValorCausa(processoAppCriacaoRequest.valorCausa());
        entidade.setObservacao(processoAppCriacaoRequest.observacao());


        return entidade;
    }

    public static ProcessoAppResponse toResponse(ProcessoApp processoApp) {
        if (processoApp == null) {
            return null;
        }


        return new ProcessoAppResponse(
                processoApp.getId(),
                processoApp.getUsuarioResponsavel() != null ? processoApp.getUsuarioResponsavel().getId() : null,
                processoApp.getUsuarioResponsavel() != null ? processoApp.getUsuarioResponsavel().getNome() : null,
                processoApp.getCliente().getId(),
                processoApp.getCliente().getNomeRazaoSocial(),
                processoApp.getDescricaoCaso(),
                processoApp.getObservacao(),
                processoApp.getPrioridade() != null ? processoApp.getPrioridade() : null,
                processoApp.getFaseProcesso() != null ? processoApp.getFaseProcesso() : null,
                processoApp.getUltimaAtualizacao(),
                processoApp.getDataHoraInicio(),
                processoApp.getAtivo(),
                processoApp.getDataAudiencia(),
                processoApp.getDataHoraEncerramento(),
                processoApp.getEncerradoPorUsuario() != null ? processoApp.getEncerradoPorUsuario().getId() : null,
                processoApp.getEncerradoPorUsuario() != null ? processoApp.getEncerradoPorUsuario().getNome() : null,
                processoApp.getSiglaTribunal() != null ? processoApp.getSiglaTribunal() : null,
                processoApp.getValorCausa(),
                ProcessoApiMapper.toResponse(processoApp.getProcessoApi())
        );
    }
}
