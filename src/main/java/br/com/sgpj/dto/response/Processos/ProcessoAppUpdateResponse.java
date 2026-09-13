package br.com.sgpj.dto.response.Processos;

import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.StatusProcesso;

import java.time.LocalDateTime;

public record ProcessoAppUpdateResponse(
        Long id,
        Long usuarioResponsavelId,
        String usuarioResponsavelNome,
        Long clienteId,
        String clientNomeRazaoSocial,
        String descricaoCaso,
        String obervacao,
        Prioridade prioridade,
        FaseProcesso faseProcesso,
        LocalDateTime ultimaAtualizacao,
        LocalDateTime dataHoraInicio,
        StatusProcesso status,
        LocalDateTime dataAudiencia,
        LocalDateTime encerradoSuspensoEm,
        Long encerradoSuspensoPorUsuarioId,
        String encerradoSuspensoPorUsuarioNome,
        Integer motivoStatusId,
        String motivoStatusNome,
        String justificativa,
        SiglaTribunal siglaTribunal,
        ProcessoApi processoApi,
        Double valorCausa
) {
}
