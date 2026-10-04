package br.com.sgpj.dto.response.Dashboard;

public record ProcessosSuspensosResponse(
        int suspensosPorAcordoParcelado,
        int suspensosPorTratativas,
        int suspensosPorAusenciaDeBens,
        int suspensosPorDeterminacaoJudicial,
        int suspensosPorOutros
) {}
