package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.adapter.api.DataJudAdapter;
import br.com.sgpj.dto.response.DataJudApiResponse;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.exception.TribunalInvalidoException;
import br.com.sgpj.mapper.ProcessoApiMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ProcessoApiGateway.class);

    private final DataJudAdapter dataJudAdapter;

    public ProcessoApiGateway(DataJudAdapter dataJudAdapter) {
        this.dataJudAdapter = dataJudAdapter;
    }

    /**
     * Busca dados oficiais do processo na API DataJud.
     *
     * @param numeroProcesso Número do processo em formato CNJ
     * siglaTribunal Tribunal responsável
     * @return ProcessoApi com dados oficiais, ou null se não encontrado
     */
    public ProcessoApi buscarDadosOficiais(String numeroProcesso, String siglaTribunal) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            return null;
        }

        String cnjLimpo = numeroProcesso.replaceAll("\\D", "");
        try {
            DataJudApiResponse dataJudResponse = dataJudAdapter.buscarPorNumeroDeProcesso(
                    cnjLimpo,
                    siglaTribunal
            );

            if (dataJudResponse == null || !dataJudResponse.temResultados()) {
                return null;
            }

            return ProcessoApiMapper.toEntity(dataJudResponse);
        } catch (Exception ex) {
            log.warn("Não foi possível obter dados oficiais do processo {} no tribunal {}: {}",
                    cnjLimpo, siglaTribunal != null ? siglaTribunal : "null", ex.getMessage());
            return null;
        }
    }
}

