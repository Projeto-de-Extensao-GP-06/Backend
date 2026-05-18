package br.com.sgpj.mapper;


import br.com.sgpj.dto.response.MovimentacaoOficialResponse;
import br.com.sgpj.dto.response.ProcessoApiResponse;
import br.com.sgpj.entity.Processo.MovimentacaoOficial;
import br.com.sgpj.entity.Processo.ProcessoApi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        processoApi.setDataAjuizamento(LocalDateTime.parse(main.getString("dataAjuizamento"), formatter));

        processoApi.setNivelSigilo(main.getInt("nivelSigilo"));
        processoApi.setOrgaoJulgador(main.getJSONObject("orgaoJulgador").getString("nome"));


        JSONArray movimentos = main.getJSONArray("movimentos");
        List<MovimentacaoOficial> movimentacoes = new ArrayList<>();

        for (int i = 0; i < movimentos.length(); i++) {
            JSONObject movimentoJson = movimentos.getJSONObject(i);

            MovimentacaoOficial movimentacao = new MovimentacaoOficial();
            movimentacao.setCodigo(movimentoJson.getString("codigo"));
            movimentacao.setDescricao(movimentoJson.getString("nome"));
            movimentacao.setDataHoraMovimentacao(LocalDateTime.parse(movimentoJson.getString("dataHora"), formatter));
            movimentacao.setProcessoApi(processoApi);

            movimentacoes.add(movimentacao);
        }

        processoApi.setMovimentacoes(movimentacoes);

        return processoApi;
    }

    public static ProcessoApiResponse toResponse(ProcessoApi processoApi) {
        if (processoApi == null) {
            return null;
        }

        return new ProcessoApiResponse(
                processoApi.getId(),
                processoApi.getNumeroCnj(),
                processoApi.getTribunalTramitacao(),
                processoApi.getObservacao(),
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

    private static List<MovimentacaoOficialResponse> toMovimentacoesResponse(List<MovimentacaoOficial> movimentacoes) {
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
