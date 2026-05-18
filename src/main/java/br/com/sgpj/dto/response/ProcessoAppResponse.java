package br.com.sgpj.dto.response;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Usuario;

import java.time.LocalDateTime;

public record ProcessoAppResponse(
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
        ProcessoApiResponse processoApi
) {
}
