package br.com.sgpj.dto.response.Dashboard;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Usuario;

import java.time.LocalDateTime;

public record AudienciasResponse (
        Long idProcesso,
        LocalDateTime dataAudiencia,
        String nomeFaseProcesso,
        String nomeUsuarioResponsavel,
        String emailUsuarioResponsavel,
        String nomePrioridade,
        String nomeCliente
)
{}
