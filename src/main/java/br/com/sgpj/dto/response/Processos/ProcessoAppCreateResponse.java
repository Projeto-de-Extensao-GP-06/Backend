package br.com.sgpj.dto.response.Processos;

import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.SiglaTribunal;

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
        Boolean ativo,
        LocalDateTime dataAudiencia,
        LocalDateTime dataHoraEncerramento,
        Long encerradoPorUsuarioId,
        String encerradoPorUsuarioNome,
        SiglaTribunal siglaTribunal,
        Double valorCausa,
        ProcessoApiCreateResponse processoApi
) {
}
