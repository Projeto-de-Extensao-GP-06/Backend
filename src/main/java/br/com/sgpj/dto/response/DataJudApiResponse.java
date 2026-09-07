package br.com.sgpj.dto.response;

import java.util.List;

/**
 * DTO que encapsula a resposta bruta da API DataJud.
 * Mantém a estrutura JSON sem transformações.
 * 
 * Usado apenas como intermediate layer entre adapter HTTP e tradutor.
 */
public record DataJudApiResponse(
        Hits hits
) {
    /**
     * Estrutura de hits da resposta DataJud.
     */
    public record Hits(
            int total,
            List<Hit> hits
    ) {}

    /**
     * Um documento individual da resposta.
     */
    public record Hit(
            String _index,
            String _id,
            double _score,
            Source _source
    ) {}

    /**
     * Dados do processo dentro de um documento.
     */
    public record Source(
            String numeroProcesso,
            String dataProcesso,
            String situacao,
            String magistrado,
            String tribunal,
            String segmento,
            String ramo,
            List<Movimentacao> movimentacoes
    ) {}

    /**
     * Movimentação de um processo.
     */
    public record Movimentacao(
            String data,
            String tipo,
            String descricao
    ) {}

    /**
     * Verifica se houve resultado na busca.
     */
    public boolean temResultados() {
        return hits != null && hits.total > 0 && !hits.hits.isEmpty();
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
