package br.com.sgpj.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO que encapsula a resposta bruta da API DataJud (CNJ).
 * Mantém a estrutura JSON retornada pelo Elasticsearch do DataJud.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DataJudApiResponse(
        Hits hits
) {
    /**
     * Estrutura de hits da resposta DataJud.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hits(
            Total total,
            Double max_score,
            List<Hit> hits
    ) {}

    /**
     * Total de registros retornados pelo Elasticsearch (DataJud).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Total(
            int value,
            String relation
    ) {}

    /**
     * Um documento individual da resposta.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hit(
            String _index,
            String _id,
            Double _score,
            Source _source
    ) {}

    /**
     * Dados do processo dentro de um documento retornado pelo DataJud.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Source(
            String numeroProcesso,
            ItemNome classe,
            ItemNome sistema,
            ItemNome formato,
            String tribunal,
            String grau,
            String dataAjuizamento,
            Integer nivelSigilo,
            ItemNome orgaoJulgador,
            List<ItemNome> assuntos,
            List<Movimento> movimentos
    ) {}

    /**
     * Representação genérica para objetos com código e nome (classe, sistema, formato, orgaoJulgador, assunto).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemNome(
            Integer codigo,
            String nome
    ) {}

    /**
     * Movimentação de um processo no DataJud.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Movimento(
            Integer codigo,
            String nome,
            String dataHora
    ) {}

    /**
     * Verifica se houve resultado na busca.
     */
    public boolean temResultados() {
        return hits != null
                && hits.total != null
                && hits.total.value > 0
                && hits.hits != null
                && !hits.hits.isEmpty();
    }

    /**
     * Retorna o primeiro resultado, se existir.
     */
    public Hit getPrimeiroResultado() {
        if (temResultados()) {
            return hits.hits.get(0);
        }
        return null;
    }
}
