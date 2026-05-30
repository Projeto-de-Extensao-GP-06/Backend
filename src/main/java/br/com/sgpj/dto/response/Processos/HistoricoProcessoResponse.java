package br.com.sgpj.dto.response.Processos;

import java.time.LocalDateTime;

public record HistoricoProcessoResponse(
        Long historicoProcessoId,
        Long processoAppId,
        Long usuarioUtilizadoId,
        String usuarioUtilizadoNome,
        String descricaoAlteracao,
        LocalDateTime dataHoraAlteracao,
        Long tipoAlteracaoId,
        String tipoAlteracaoNome
) {
}
