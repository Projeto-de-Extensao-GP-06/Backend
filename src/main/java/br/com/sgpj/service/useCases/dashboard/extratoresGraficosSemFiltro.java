package br.com.sgpj.service.useCases.dashboard;

import br.com.sgpj.dto.response.Dashboard.AudienciasResponse;
import br.com.sgpj.dto.response.Dashboard.ProcessosPorFaseResponse;
import br.com.sgpj.dto.response.Dashboard.TempoDuracaoProcessosResponse;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class extratoresGraficosSemFiltro {
    public List<ProcessosPorFaseResponse> extrairProcessosPorFase(
            List<ProcessoApp> processos,
            List<FaseProcesso> fasesProcessos
    ){
        List<ProcessosPorFaseResponse> processosPorFase = new ArrayList<>();

        for (FaseProcesso faseAtual : fasesProcessos) {
            int qtdProcessoFaseAtual = 0;

            for (ProcessoApp processo : processos) {
                if (processo.getFaseProcesso().equals(faseAtual)){
                    qtdProcessoFaseAtual++;
                }
            }

            processosPorFase.add(new ProcessosPorFaseResponse(faseAtual.getNome(), qtdProcessoFaseAtual));
        }

        return processosPorFase;
    }


    public List<AudienciasResponse> extrairAudiencias(List<ProcessoApp> processos){

        List<AudienciasResponse> audiencia = new ArrayList<>();

        for (ProcessoApp processo : processos) {
            if (
                    processo.getDataAudiencia() != null &&
                            !processo.getDataAudiencia().isBefore(LocalDateTime.now())
            ) {
                audiencia.add(new AudienciasResponse(
                                processo.getId(),
                                processo.getDataAudiencia(),
                                processo.getFaseProcesso().getNome(),
                                processo.getUsuarioResponsavel().getNome(),
                                processo.getUsuarioResponsavel().getEmail(),
                                processo.getPrioridade().getNome(),
                                processo.getCliente().getNomeRazaoSocial()
                        )
                );
            }
        }
        audiencia.sort(Comparator.comparing(AudienciasResponse::dataAudiencia));
        return audiencia;
    }


    public TempoDuracaoProcessosResponse extrairTempoDuracaoProcessos(List<ProcessoApp> processos){

        int ate6Meses = 0;
        int de6a12Meses = 0;
        int de1a2Anos = 0;
        int de2a5Anos = 0;
        int mais5Anos = 0;

        for (ProcessoApp processo : processos) {
            if (processo.getDataHoraInicio() == null) continue;

            long meses = ChronoUnit.MONTHS.between(processo.getDataHoraInicio(), LocalDateTime.now());

            if (meses < 6) {
                ate6Meses++;
            } else if (meses < 12) {
                de6a12Meses++;
            } else if (meses < 24) {
                de1a2Anos++;
            } else if (meses < 60) {
                de2a5Anos++;
            } else {
                mais5Anos++;
            }
        }

        return new TempoDuracaoProcessosResponse(ate6Meses, de6a12Meses, de1a2Anos, de2a5Anos, mais5Anos);
    }
}
