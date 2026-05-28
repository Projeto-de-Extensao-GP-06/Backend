package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.FaseProcessoResponse;
import br.com.sgpj.entity.Processo.FaseProcesso;

public class FaseProcessoMapper {

    public static FaseProcessoResponse toResponse(FaseProcesso faseProcesso){

        return new FaseProcessoResponse(faseProcesso.getId(), faseProcesso.getNome());
    }
}
