package br.com.sgpj.entity.Processo;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record ProcessoDocumentoId(
        Long documentoId,
        Long processoAppId

) implements Serializable {
}
