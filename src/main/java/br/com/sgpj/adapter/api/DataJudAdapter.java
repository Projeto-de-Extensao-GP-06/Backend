package br.com.sgpj.adapter.api;

import br.com.sgpj.adapter.api.translator.DataJudJsonTranslator;
import br.com.sgpj.dto.response.DataJudApiResponse;
import br.com.sgpj.exception.DataJudIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class DataJudAdapter {

    private final RestClient restClient;
    private final DataJudJsonTranslator jsonTranslator;
    private final String dataJudApiKey;

    public DataJudAdapter(
            RestClient restClient,
            DataJudJsonTranslator jsonTranslator,
            @Value("${datajud.api-key}") String dataJudApiKey
    ) {
        this.restClient = restClient;
        this.jsonTranslator = jsonTranslator;
        this.dataJudApiKey = dataJudApiKey;
    }


    public DataJudApiResponse buscarPorNumeroDeProcesso(String cnj, String siglaTribunal) {
        String endpoint = "/api_publica_" + siglaTribunal.toLowerCase() + "/_search";

        Map<String, Object> body = Map.of(
                "query", Map.of(
                        "match", Map.of(
                                "numeroProcesso", cnj
                        )
                )
        );

        try {
            // Faz a requisição HTTP e retorna a resposta como String
            String jsonResponse = restClient.post()
                    .uri(endpoint)
                    .header("Authorization", dataJudApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            // Delega o parsing JSON para o tradutor
            return parseResponse(jsonResponse);

        } catch (ResourceAccessException ex) {
            throw new DataJudIntegrationException(
                    "Não foi possível conectar à API DataJud: " + ex.getMessage(),
                    ex
            );
        } catch (Exception ex) {
            throw new DataJudIntegrationException(
                    "Erro ao buscar processo na API DataJud: " + ex.getMessage(),
                    ex
            );
        }
    }



    private DataJudApiResponse parseResponse(String jsonResponse) {
        DataJudApiResponse response = jsonTranslator.translateToDto(jsonResponse);
        jsonTranslator.validarEstrutura(response);
        return response;
    }
}

