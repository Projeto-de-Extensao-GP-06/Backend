package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.adapter.api.DataJudAdapter;
import br.com.sgpj.dto.response.DataJudApiResponse;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.mapper.ProcessoApiMapper;
import org.springframework.stereotype.Component;

/**
 * Gateway que orquestra a busca de dados oficiais de processos.
 * 
 * Fluxo:
 * 1. Recebe número do processo e tribunal
 * 2. Consulta DataJud via adapter
 * 3. Traduz resposta DTO para entidade de domínio
 */
@Component
public class ProcessoApiGateway {

    private final DataJudAdapter dataJudAdapter;

    public ProcessoApiGateway(DataJudAdapter dataJudAdapter) {
        this.dataJudAdapter = dataJudAdapter;
    }

    /**
     * Busca dados oficiais do processo na API DataJud.
     *
     * @param numeroProcesso Número do processo em formato CNJ
     * @param siglaTribunal Tribunal responsável
     * @return ProcessoApi com dados oficiais, ou null se não encontrado
     */
    public ProcessoApi buscarDadosOficiais(String numeroProcesso, SiglaTribunal siglaTribunal) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            return null;
        }

        // Adapter retorna DTO estruturado (não mais String JSON)
        DataJudApiResponse dataJudResponse = dataJudAdapter.buscarPorNumeroDeProcesso(
                numeroProcesso,
                siglaTribunal.getNome()
        );

        // Se não houver resultados, retorna null
        if (!dataJudResponse.temResultados()) {
            return null;
        }

        // Mapeia o DTO para a entidade de domínio
        return ProcessoApiMapper.toEntity(dataJudResponse);
    }
}

