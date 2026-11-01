package br.com.sgpj.adapter.api;

import br.com.sgpj.adapter.api.translator.DataJudJsonTranslator;
import br.com.sgpj.dto.response.DataJudApiResponse;
import br.com.sgpj.exception.DataJudIntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class DataJudAdapter {

    private static final Logger log = LoggerFactory.getLogger(DataJudAdapter.class);

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
        if (cnj == null || cnj.isBlank() || siglaTribunal == null || siglaTribunal.isBlank()) {
            return null;
        }

        String cnjLimpo = cnj.replaceAll("\\D", "");
        String endpoint = "/api_publica_" + siglaTribunal.toLowerCase().trim() + "/_search";

        Map<String, Object> body = Map.of(
                "query", Map.of(
                        "match", Map.of(
                                "numeroProcesso", cnjLimpo
                        )
                )
        );

        try {
            String jsonResponse = restClient.post()
                    .uri(endpoint)
                    .header("Authorization", dataJudApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseResponse(jsonResponse);
        } catch (ResourceAccessException ex) {
            log.warn("Consulta ao DataJud para o processo {} falhou por timeout/conexão: {}", cnjLimpo, ex.getMessage());
            throw new DataJudIntegrationException(
                    "Não foi possível conectar ao DataJud (tempo limite esgotado).",
                    ex
            );
        } catch (DataJudIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Consulta ao DataJud para o processo {} falhou: {}", cnjLimpo, ex.getMessage());
            throw new DataJudIntegrationException(
                    "Não foi possível estabelecer conexão com o DataJud.",
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
