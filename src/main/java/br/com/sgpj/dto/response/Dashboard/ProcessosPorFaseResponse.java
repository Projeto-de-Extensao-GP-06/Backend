package br.com.sgpj.dto.response.Dashboard;

public record ProcessosPorFaseResponse(
        String nomeFase,
        Integer qtdProcessos
) {}
