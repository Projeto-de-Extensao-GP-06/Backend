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

        int maxTentativas = 2;
        long esperaEntreTentativasMs = 2000;
        Exception ultimaExcecao = null;
        DataJudApiResponse ultimaResposta = null;

        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            try {
                String jsonResponse = restClient.post()
                        .uri(endpoint)
                        .header("Authorization", dataJudApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);

                DataJudApiResponse response = parseResponse(jsonResponse);
                if (response != null && response.temResultados()) {
                    return response;
                }

                ultimaResposta = response;
                // Se retornou 0 resultados, pode ter sido fila cheia ou falha temporária nos nós do tribunal
                if (tentativa < maxTentativas) {
                    log.info("Tentativa {}/{} no DataJud para o processo {} retornou 0 resultados. Aguardando 2s antes da próxima tentativa...",
                            tentativa, maxTentativas, cnjLimpo);
                    Thread.sleep(esperaEntreTentativasMs);
                }

            } catch (Exception ex) {
                ultimaExcecao = ex;
                log.warn("Tentativa {}/{} no DataJud para o processo {} falhou: {}",
                        tentativa, maxTentativas, cnjLimpo, ex.getMessage());

                if (tentativa < maxTentativas) {
                    try {
                        Thread.sleep(esperaEntreTentativasMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new DataJudIntegrationException("Execução interrompida durante o retry", e);
                    }
                }
            }
        }

        if (ultimaResposta != null) {
            return ultimaResposta;
        }

        if (ultimaExcecao instanceof ResourceAccessException ex) {
            throw new DataJudIntegrationException(
                    "Não foi possível conectar ao DataJud (tempo limite esgotado).",
                    ex
            );
        }

        throw new DataJudIntegrationException(
                "Não foi possível estabelecer conexão com o DataJud.",
                ultimaExcecao
        );
    }

    private DataJudApiResponse parseResponse(String jsonResponse) {
        DataJudApiResponse response = jsonTranslator.translateToDto(jsonResponse);
        jsonTranslator.validarEstrutura(response);
        return response;
    }
}
