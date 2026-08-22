package br.com.sgpj.adapter.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;


@Component
public class DataJudAdapter {

    private final RestClient restClient;
    private final String dataJudApiKey;

    public DataJudAdapter(
            RestClient restClient,
            @Value("${datajud.api-key}") String dataJudApiKey
    ) {
        this.restClient = restClient;
        this.dataJudApiKey = dataJudApiKey;
    }


        public String buscarPorNumeroDeProcesso(String cnj, String siglaTribunal) {

            String endpoint =
                    "/api_publica_" + siglaTribunal.toLowerCase() + "/_search";

            Map<String, Object> body = Map.of(
                    "query", Map.of(
                            "match", Map.of(
                                    "numeroProcesso", cnj
                            )
                    )
            );

            return restClient.post()
                    .uri(endpoint)
                    .header("Authorization", "APIKey " + dataJudApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
    }
}
