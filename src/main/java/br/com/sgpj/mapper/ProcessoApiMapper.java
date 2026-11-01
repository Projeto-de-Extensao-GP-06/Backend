package br.com.sgpj.mapper;


import br.com.sgpj.dto.response.DataJudApiResponse;
import br.com.sgpj.dto.response.MovimentacaoOficialResponse;
import br.com.sgpj.dto.response.Processos.ProcessoApiCreateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoApiResponse;
import br.com.sgpj.entity.Processo.MovimentacoesOficiais;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.exception.DataJudIntegrationException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper que converte resposta DTO da API DataJud para entidades de domínio.
 * 
 * Responsável por:
 * - Extrair dados do DTO estruturado
 * - Transformar tipos de dados
 * - Validar dados obrigatórios
 */
public class ProcessoApiMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int HISTORICO_MOVIMENTACOES = 10;

    /**
     * Converte resposta DTO da API DataJud para entidade ProcessoApi.
     *
     * @param dataJudResponse DTO com resposta da API DataJud
     * @return ProcessoApi mapeado
     * @throws DataJudIntegrationException se não houver dados ou estrutura inválida
     */
    public static ProcessoApi toEntity(DataJudApiResponse dataJudResponse) {
        if (!dataJudResponse.temResultados()) {
            throw new DataJudIntegrationException(
                "Nenhum resultado encontrado na resposta DataJud"
            );
        }

        // Extrai o primeiro resultado
        DataJudApiResponse.Hit resultado = dataJudResponse.getPrimeiroResultado();
        DataJudApiResponse.Source source = resultado._source();

        ProcessoApi processoApi = new ProcessoApi();

        // Mapeia dados básicos
        processoApi.setNumeroCnj(limitarTamanho(source.numeroProcesso(), 25));
        processoApi.setClasseProcessual(source.classe() != null ? limitarTamanho(source.classe().nome(), 255) : null);
        processoApi.setSistemaTribunal(source.sistema() != null ? limitarTamanho(source.sistema().nome(), 100) : null);
        processoApi.setFormato(source.formato() != null ? limitarTamanho(source.formato().nome(), 50) : null);
        processoApi.setTribunalTramitacao(limitarTamanho(source.tribunal(), 100));
        processoApi.setInstancia(limitarTamanho(source.grau(), 100));

        if (source.assuntos() != null && !source.assuntos().isEmpty() && source.assuntos().get(0) != null) {
            processoApi.setAssunto(limitarTamanho(source.assuntos().get(0).nome(), 255));
        }

        processoApi.setNivelSigilo(source.nivelSigilo() != null ? String.valueOf(source.nivelSigilo()) : "0");
        processoApi.setOrgaoJulgador(source.orgaoJulgador() != null ? limitarTamanho(source.orgaoJulgador().nome(), 255) : "Não informado");

        // Mapeia data de ajuizamento
        try {
            if (source.dataAjuizamento() != null && !source.dataAjuizamento().isBlank()) {
                processoApi.setDataAjuizamento(
                    LocalDateTime.parse(source.dataAjuizamento(), FORMATTER)
                );
            }
        } catch (Exception e) {
            // Se falhar ao parsear data, deixa como null
            processoApi.setDataAjuizamento(null);
        }

        // Mapeia movimentações
        List<MovimentacoesOficiais> movimentacoes = mapearMovimentacoes(source.movimentos(), processoApi);
        
        // Mantém apenas as últimas 10 movimentações
        int inicio = Math.max(0, movimentacoes.size() - HISTORICO_MOVIMENTACOES);
        processoApi.setMovimentacoes(movimentacoes.subList(inicio, movimentacoes.size()));

        return processoApi;
    }

    /**
     * Mapeia lista de movimentações do DTO para entidades de domínio.
     */
    private static List<MovimentacoesOficiais> mapearMovimentacoes(
            List<DataJudApiResponse.Movimento> movimentosDto,
            ProcessoApi processoApi
    ) {
        List<MovimentacoesOficiais> movimentacoes = new ArrayList<>();

        if (movimentosDto != null) {
            for (DataJudApiResponse.Movimento mov : movimentosDto) {
                try {
                    MovimentacoesOficiais movimentacao = new MovimentacoesOficiais();
                    movimentacao.setCodigo(mov.codigo() != null ? mov.codigo() : 0);
                    movimentacao.setDescricao(mov.nome());
                    
                    if (mov.dataHora() != null) {
                        movimentacao.setDataHoraMovimentacao(
                            Instant.parse(mov.dataHora())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                        );
                    }
                    
                    movimentacao.setProcessoApi(processoApi);
                    movimentacoes.add(movimentacao);
                } catch (Exception e) {
                    // Ignora movimentações com erro de parsing
                    continue;
                }
            }
        }

        return movimentacoes;
    }

    public static ProcessoApiCreateResponse toCreateResponse(ProcessoApi processoApi) {
        if (processoApi == null) {
            return null;
        }

        return new ProcessoApiCreateResponse(
                processoApi.getId(),
                processoApi.getNumeroCnj(),
                processoApi.getTribunalTramitacao(),
                processoApi.getInstancia(),
                processoApi.getDataAjuizamento(),
                processoApi.getNivelSigilo(),
                processoApi.getClasseProcessual(),
                processoApi.getFormato(),
                processoApi.getSistemaTribunal(),
                processoApi.getOrgaoJulgador(),
                processoApi.getAssunto(),
                toMovimentacoesResponse(processoApi.getMovimentacoes())
        );
    }


    public static ProcessoApiResponse toResponse(ProcessoApi processoApi) {
        if (processoApi == null) {
            return null;
        }

        List<MovimentacoesOficiais> movimentacoes = processoApi.getMovimentacoes();
        MovimentacaoOficialResponse ultimoMovimento = (movimentacoes != null && !movimentacoes.isEmpty())
                ? toMovimentacaoResponse(movimentacoes.getLast())
                : null;

        return new ProcessoApiResponse(
                processoApi.getId(),
                processoApi.getNumeroCnj(),
                processoApi.getTribunalTramitacao(),
                processoApi.getInstancia(),
                processoApi.getDataAjuizamento(),
                processoApi.getNivelSigilo(),
                processoApi.getClasseProcessual(),
                processoApi.getFormato(),
                processoApi.getSistemaTribunal(),
                processoApi.getOrgaoJulgador(),
                processoApi.getAssunto(),
                ultimoMovimento
        );
    }

    public static MovimentacaoOficialResponse toMovimentacaoResponse(MovimentacoesOficiais movimentacoesOficiais){

        return new MovimentacaoOficialResponse(
                movimentacoesOficiais.getId(),
                movimentacoesOficiais.getCodigo(),
                movimentacoesOficiais.getDescricao(),
                movimentacoesOficiais.getDataHoraMovimentacao()
        );
    }

    private static List<MovimentacaoOficialResponse> toMovimentacoesResponse(List<MovimentacoesOficiais> movimentacoes) {
        if (movimentacoes == null) {
            return List.of();
        }

        return movimentacoes.stream()
                .map(movimentacao -> new MovimentacaoOficialResponse(
                        movimentacao.getId(),
                        movimentacao.getCodigo(),
                        movimentacao.getDescricao(),
                        movimentacao.getDataHoraMovimentacao()
                ))
                .toList();
    }

    private static String limitarTamanho(String texto, int tamanhoMaximo) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.length() <= tamanhoMaximo ? limpo : limpo.substring(0, tamanhoMaximo);
    }
}
