package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.MotivoStatusProcessoResponse;
import br.com.sgpj.entity.Processo.MotivoStatusProcesso;

public class MotivoStatusProcessoMapper {

    public static MotivoStatusProcessoResponse toResponse(MotivoStatusProcesso motivo) {
        return new MotivoStatusProcessoResponse(
                motivo.getId(),
                motivo.getNome(),
                motivo.getAplicavelA() != null ? motivo.getAplicavelA().name() : null
        );
    }
}
