package br.com.sgpj.dto.response.Dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record DadosIndependentesResponse(

    @Schema(description = "Número de processos ativos", example = "2")
    Integer processoAtivos,

    @Schema(description = "Número de solicitações pendentes", example = "2")
    Integer solicitacoesPendentes,

    @Schema(description = "Número de clientes existentes", example = "2")
    Integer clientesExistentes,

    @Schema(description = "Número de processos com audiência marcada", example = "1")
    Integer processosComAudiencia,

    @Schema(description = "Contagem de processos por fase")
    List<ProcessosPorFaseResponse> processosPorFase,

    @Schema(description = "Contagem de processos que estão em cada faixa de tempo de duração.")
    TempoDuracaoProcessosResponse tempoDuracaoProcessos,

    @Schema(description = "Lista de processos com audiência marcada (ordem de prioridade)")
    List<AudienciasResponse> audiencias
    )
{}
