package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.adapter.api.DataJudAdapter;
import br.com.sgpj.dto.response.DataJudApiResponse;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.exception.DataJudIntegrationException;
import br.com.sgpj.mapper.ProcessoApiMapper;
import org.springframework.stereotype.Component;

/**
 * Gateway que orquestra a busca de dados oficiais de processos.
 * 
 * Fluxo:
 * 1. Recebe número do processo e tribunal
 * 2. Consulta DataJud via adapter
 * 3. Traduz resposta DTO para entidade de domínio
 *
 * Não há retry nem fallback aqui: falhas são propagadas e a política de retentativa
 * é responsabilidade exclusiva da fila RabbitMQ de sincronização.
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
     * @return ProcessoApi com dados oficiais
     * @throws DataJudIntegrationException se a consulta falhar ou não retornar resultados
     */
    public ProcessoApi buscarDadosOficiaisOuFalhar(String numeroProcesso, String siglaTribunal) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            throw new DataJudIntegrationException("Número do processo não informado.");
        }

        String cnjLimpo = numeroProcesso.replaceAll("\\D", "");
        DataJudApiResponse dataJudResponse = dataJudAdapter.buscarPorNumeroDeProcesso(cnjLimpo, siglaTribunal);

        if (dataJudResponse == null || !dataJudResponse.temResultados()) {
            throw new DataJudIntegrationException("DataJud não retornou resultados para o processo " + cnjLimpo + ".");
        }

        return ProcessoApiMapper.toEntity(dataJudResponse);
    }
}
