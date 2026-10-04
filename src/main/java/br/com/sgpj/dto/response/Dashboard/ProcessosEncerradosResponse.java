package br.com.sgpj.dto.response.Dashboard;

public record ProcessosEncerradosResponse (
        int encerradosPorAcordo,
        int encerradosPorSemResolucao,
        int encerradosPorPenhoraTotal,
        int encerradosPorPagamentoVoluntario,
        int encerradosPorOutros
){}
