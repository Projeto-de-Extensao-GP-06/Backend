package br.com.sgpj.mapper;


import br.com.sgpj.dto.response.MovimentacaoOficialResponse;
import br.com.sgpj.dto.response.Processos.ProcessoApiCreateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoApiResponse;
import br.com.sgpj.entity.Processo.MovimentacoesOficiais;
import br.com.sgpj.entity.Processo.ProcessoApi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProcessoApiMapper {

    public static ProcessoApi toEntity(JSONObject json){

        ProcessoApi processoApi = new ProcessoApi();

        JSONObject main = json.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getJSONObject("_source");

        processoApi.setNumeroCnj(main.getString("numeroProcesso"));
        processoApi.setClasseProcessual(main.getJSONObject("classe").getString("nome"));
        processoApi.setSistemaTribunal(main.getJSONObject("sistema").getString("nome"));
        processoApi.setFormato(main.getJSONObject("formato").getString("nome"));
        processoApi.setTribunalTramitacao(main.getString("tribunal"));
        processoApi.setInstancia(main.getString("grau"));
        processoApi.setAssunto(main.getJSONArray("assuntos").getJSONObject(0).getString("nome"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        processoApi.setDataAjuizamento(LocalDateTime.parse(main.getString("dataAjuizamento"), formatter));

        processoApi.setNivelSigilo(main.getInt("nivelSigilo"));
        processoApi.setOrgaoJulgador(main.getJSONObject("orgaoJulgador").getString("nome"));


        JSONArray movimentos = main.getJSONArray("movimentos");
        List<MovimentacoesOficiais> movimentacoes = new ArrayList<>();

        for (int i = 0; i < movimentos.length(); i++) {
            JSONObject movimentoJson = movimentos.getJSONObject(i);

            MovimentacoesOficiais movimentacao = new MovimentacoesOficiais();
            movimentacao.setCodigo(movimentoJson.getInt("codigo"));
            movimentacao.setDescricao(movimentoJson.getString("nome"));
            movimentacao.setDataHoraMovimentacao(
                    Instant.parse(movimentoJson.getString("dataHora")).atZone(ZoneId.systemDefault()).toLocalDateTime()
            );
            movimentacao.setProcessoApi(processoApi);

            movimentacoes.add(movimentacao);
        }

        processoApi.setMovimentacoes(movimentacoes);

        return processoApi;
    }

    public static ProcessoApiCreateResponse toCreateResponse(ProcessoApi processoApi) {
        if (processoApi == null) {
            return null;
        }

        return new ProcessoApiCreateResponse(
                processoApi.getId(),
                processoApi.getNumeroCnj(),
                processoApi.getTribunalTramitacao(),
                processoApi.getInstancia(),
                processoApi.getDataAjuizamento(),
                processoApi.getNivelSigilo(),
                processoApi.getClasseProcessual(),
                processoApi.getFormato(),
                processoApi.getSistemaTribunal(),
                processoApi.getOrgaoJulgador(),
                processoApi.getAssunto(),
                toMovimentacoesResponse(processoApi.getMovimentacoes())
        );
    }


    public static ProcessoApiResponse toResponse(ProcessoApi processoApi) {
        if (processoApi == null) {
            return null;
        }

        return new ProcessoApiResponse(
                processoApi.getId(),
                processoApi.getNumeroCnj(),
                processoApi.getTribunalTramitacao(),
                processoApi.getInstancia(),
                processoApi.getDataAjuizamento(),
                processoApi.getNivelSigilo(),
                processoApi.getClasseProcessual(),
                processoApi.getFormato(),
                processoApi.getSistemaTribunal(),
                processoApi.getOrgaoJulgador(),
                processoApi.getAssunto(),
                toMovimentacaoResponse(processoApi.getMovimentacoes().getLast())
        );
    }

    public static MovimentacaoOficialResponse toMovimentacaoResponse(MovimentacoesOficiais movimentacoesOficiais){

        return new MovimentacaoOficialResponse(
                movimentacoesOficiais.getId(),
                movimentacoesOficiais.getCodigo(),
                movimentacoesOficiais.getDescricao(),
                movimentacoesOficiais.getDataHoraMovimentacao()
        );
    }

    private static List<MovimentacaoOficialResponse> toMovimentacoesResponse(List<MovimentacoesOficiais> movimentacoes) {
        if (movimentacoes == null) {
            return List.of();
        }


        return movimentacoes.stream()
                .map(movimentacao -> new MovimentacaoOficialResponse(
                        movimentacao.getId(),
                        movimentacao.getCodigo(),
                        movimentacao.getDescricao(),
                        movimentacao.getDataHoraMovimentacao()
                ))
                .toList();
    }
}
