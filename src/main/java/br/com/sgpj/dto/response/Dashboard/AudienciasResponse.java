package br.com.sgpj.dto.response.Dashboard;

import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Usuario;

import java.time.LocalDateTime;

public record AudienciasResponse (
        LocalDateTime dataAudiencia,
        FaseProcesso faseProcesso,
        Usuario usuarioResponsavel,
        Prioridade prioridade
)
{}
