package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.entity.Processo.HistoricoProcesso;


public class HistoricoProcessoMapper {

    public static HistoricoProcessoResponse toResponse(HistoricoProcesso novoHistorico){


        return new HistoricoProcessoResponse(
                novoHistorico.getId(),
                novoHistorico.getProcessoApp().getId(),
                novoHistorico.getUsuarioUtilizado().getId(),
                novoHistorico.getUsuarioUtilizado().getNome(),
                novoHistorico.getDescricaoAlteracao(),
                novoHistorico.getDataHoraAlteracao(),
                novoHistorico.getTipoAlteracao().getId(),
                novoHistorico.getTipoAlteracao().getNome()
        );

    }
}
