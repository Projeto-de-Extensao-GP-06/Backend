package br.com.sgpj.dto.response.Dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

public record DadosFiltradosDashResponse(

    @Schema(description = "Contagem de processos encerrados em cada categoria existente no período selecionado")
    ProcessosEncerradosResponse processosEncerrados,

    @Schema(description = "Contagem de processos suspensos em cada categoria existente no período selecionado")
    ProcessosSuspensosResponse processosSuspensos,

    @Schema(description = "Contagem de clientes que entraram por cada categoria de forma entrada existente no período selecionado")
    FormasDeEntradaResponse formasDeEntrada,

    @Schema(description = "Contagem de processos que estiveram ativos, suspensos ou encerrados em cada periodo do filtro selecionado")
    EvolucaoCarteiraResponse evolucaoCarteira

    ) {}
