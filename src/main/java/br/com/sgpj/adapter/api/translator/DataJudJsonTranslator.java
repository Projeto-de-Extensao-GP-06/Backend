package br.com.sgpj.adapter.api.translator;

import br.com.sgpj.dto.response.DataJudApiResponse;
import br.com.sgpj.exception.DataJudIntegrationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Tradutor responsável por converter respostas JSON da API DataJud
 * em DTOs estruturados.
 * 
 * Encapsula toda a lógica de parsing JSON, tratamento de erros e
 * validação de estrutura da resposta.
 */
@Component
public class DataJudJsonTranslator {

    private final ObjectMapper objectMapper;

    public DataJudJsonTranslator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Traduz uma string JSON para o DTO DataJudApiResponse.
     *
     * @param json String contendo a resposta JSON da API DataJud
     * @return DataJudApiResponse com os dados parseados
     * @throws DataJudIntegrationException se o JSON for inválido ou não puder ser parseado
     */
    public DataJudApiResponse translateToDto(String json) {
        if (json == null || json.isBlank()) {
            throw new DataJudIntegrationException(
                "Resposta vazia da API DataJud"
            );
        }

        try {
            return objectMapper.readValue(json, DataJudApiResponse.class);
        } catch (Exception e) {
            throw new DataJudIntegrationException(
                "Erro ao fazer parsing da resposta JSON: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Valida se a resposta contém dados esperados.
     *
     * @param response DTO da resposta
     * @throws DataJudIntegrationException se a resposta não possui estrutura válida
     */
    public void validarEstrutura(DataJudApiResponse response) {
        if (response == null) {
            throw new DataJudIntegrationException(
                "Resposta DataJud é nula"
            );
        }

        if (response.hits() == null) {
            throw new DataJudIntegrationException(
                "Resposta DataJud não contém a estrutura esperada (falta 'hits')"
            );
        }
    }
}
