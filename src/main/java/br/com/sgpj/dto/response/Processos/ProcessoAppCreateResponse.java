package br.com.sgpj.dto.response.Processos;

import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.StatusProcesso;

import java.time.LocalDateTime;

public record ProcessoAppCreateResponse(
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
        Double valorCausa,
        ProcessoApiCreateResponse processoApi,
        String numeroProcesso,
        Boolean sincronizado
) {
    public ProcessoAppCreateResponse(
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
            Double valorCausa,
            ProcessoApiCreateResponse processoApi
    ) {
        this(
                id,
                usuarioResponsavelId,
                usuarioResponsavelNome,
                clienteId,
                clientNomeRazaoSocial,
                descricaoCaso,
                obervacao,
                prioridade,
                faseProcesso,
                ultimaAtualizacao,
                dataHoraInicio,
                status,
                dataAudiencia,
                encerradoSuspensoEm,
                encerradoSuspensoPorUsuarioId,
                encerradoSuspensoPorUsuarioNome,
                motivoStatusId,
                motivoStatusNome,
                justificativa,
                siglaTribunal,
                valorCausa,
                processoApi,
                processoApi != null ? processoApi.numeroCnj() : null,
                processoApi != null && processoApi.classeProcessual() != null
        );
    }
}
