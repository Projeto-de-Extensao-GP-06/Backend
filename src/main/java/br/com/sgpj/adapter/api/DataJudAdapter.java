package br.com.sgpj.adapter.api;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;


@Component
public class DataJudAdapter {

    private final RestClient restClient;

    public DataJudAdapter(RestClient restClient) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api-publica.datajud.cnj.jus.br")
                .build();
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
                    .header("Authorization", "APIKey cDZHYzlZa0JadVREZDJCendQbXY6SkJlTzNjLV9TRENyQk1RdnFKZGRQdw==")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
    }
}
