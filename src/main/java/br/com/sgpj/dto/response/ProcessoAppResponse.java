package br.com.sgpj.dto.response;

import java.time.LocalDateTime;

public record ProcessoAppResponse(
        Long id,
        Long usuarioResponsavelId,
        String usuarioResponsavelNome,
        String descricaoCaso,
        Long prioridadeId,
        String prioridadeNome,
        Long faseProcessoId,
        String faseProcessoNome,
        LocalDateTime ultimaAtualizacao,
        LocalDateTime dataHoraInicio,
        Boolean ativo,
        LocalDateTime dataAudiencia,
        LocalDateTime dataHoraEncerramento,
        Long encerradoPorUsuarioId,
        String encerradoPorUsuarioNome,
        Long siglaTribunalId,
        String siglaTribunalNome,
        Double valorCausa,
        ProcessoApiResponse processoApi
) {
}
