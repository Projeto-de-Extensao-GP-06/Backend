package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.SiglaTribunalResponse;
import br.com.sgpj.entity.Processo.SiglaTribunal;

public class SiglaTribunalMapper {

    public static SiglaTribunalResponse toResponse(SiglaTribunal siglaTribunal){

        return new SiglaTribunalResponse(siglaTribunal.getId(), siglaTribunal.getNome());
    }
}
