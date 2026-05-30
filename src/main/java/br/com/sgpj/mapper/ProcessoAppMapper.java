package br.com.sgpj.mapper;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.*;
import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;

import java.time.LocalDateTime;
import java.util.List;

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

    public static ProcessoApp toUpdateEntity(ProcessoAppUpdateRequest processoAppUpdateRequest) {

        ProcessoApp entidade = new ProcessoApp();

        entidade.setDescricaoCaso(processoAppUpdateRequest.descricaoCaso());
        entidade.setObservacao(processoAppUpdateRequest.observacao());
        entidade.setUltimaAtualizacao(LocalDateTime.now());
        entidade.setAtivo(processoAppUpdateRequest.ativo());
        entidade.setDataAudiencia(processoAppUpdateRequest.dataAudiencia());
        entidade.setValorCausa(processoAppUpdateRequest.valorCausa());



        return entidade;
    }

    public static ProcessoAppCreateResponse toCreateResponse(ProcessoApp processoApp) {
        if (processoApp == null) {
            return null;
        }


        return new ProcessoAppCreateResponse(
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
                processoApp.getEncerradoEm(),
                processoApp.getEncerradoPorUsuario() != null ? processoApp.getEncerradoPorUsuario().getId() : null,
                processoApp.getEncerradoPorUsuario() != null ? processoApp.getEncerradoPorUsuario().getNome() : null,
                processoApp.getSiglaTribunal() != null ? processoApp.getSiglaTribunal() : null,
                processoApp.getValorCausa(),
                ProcessoApiMapper.toCreateResponse(processoApp.getProcessoApi())
        );
    }

    public static ProcessoAppUpdateResponse toUpdateResponse(ProcessoApp processoApp) {
        if (processoApp == null) {
            return null;
        }


        return new ProcessoAppUpdateResponse(
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
                processoApp.getEncerradoEm(),
                processoApp.getEncerradoPorUsuario() != null ? processoApp.getEncerradoPorUsuario().getId() : null,
                processoApp.getEncerradoPorUsuario() != null ? processoApp.getEncerradoPorUsuario().getNome() : null,
                processoApp.getSiglaTribunal() != null ? processoApp.getSiglaTribunal() : null,
                processoApp.getProcessoApi() != null ? processoApp.getProcessoApi() : null,
                processoApp.getValorCausa()
        );
    }



    public static ProcessoAppMiniResponse toMiniResponse(ProcessoApp processoApp){
        if (processoApp == null) {
            return null;
        }


        return new ProcessoAppMiniResponse(
                processoApp.getId(),
                processoApp.getCliente().getId(),
                processoApp.getCliente().getNomeRazaoSocial(),
                processoApp.getPrioridade() != null ? processoApp.getPrioridade() : null,
                processoApp.getFaseProcesso() != null ? processoApp.getFaseProcesso() : null,
                processoApp.getAtivo(),
                processoApp.getDataAudiencia(),
                processoApp.getSiglaTribunal() != null ? processoApp.getSiglaTribunal() : null,
                processoApp.getProcessoApi() != null ? processoApp.getProcessoApi().getNumeroCnj() : null
        );
    }


    public static ProcessoAppResponse toResponse(ProcessoApp processoApp, List<HistoricoProcessoResponse> historicoProcesso){

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
                      processoApp.getEncerradoEm(),
                      processoApp.getEncerradoPorUsuario() != null ? processoApp.getEncerradoPorUsuario().getId() : null,
                      processoApp.getEncerradoPorUsuario() != null ? processoApp.getEncerradoPorUsuario().getNome() : null,
                      processoApp.getSiglaTribunal() != null ? processoApp.getSiglaTribunal() : null,
                      processoApp.getValorCausa(),
                      processoApp.getProcessoApi() != null ? ProcessoApiMapper.toResponse(processoApp.getProcessoApi()) : null,
                      historicoProcesso
              );

    }
}
