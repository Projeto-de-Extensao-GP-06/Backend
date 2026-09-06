package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.adapter.api.DataJudAdapter;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.mapper.ProcessoApiMapper;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ProcessoApiGateway {

    private final DataJudAdapter dataJudAdapter;

    public ProcessoApiGateway(DataJudAdapter dataJudAdapter) {
        this.dataJudAdapter = dataJudAdapter;
    }

    public ProcessoApi buscarDadosOficiais(String numeroProcesso, SiglaTribunal siglaTribunal) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            return null;
        }

        String dataJudResponse = dataJudAdapter.buscarPorNumeroDeProcesso(numeroProcesso, siglaTribunal.getNome());
        JSONObject jsonDataJud = new JSONObject(dataJudResponse);

        return ProcessoApiMapper.toEntity(jsonDataJud);
    }

}
