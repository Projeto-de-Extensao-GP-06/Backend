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
    public ProcessoApi buscarDadosOficiais(String numeroProcesso) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            return null;
        }

        String cnjLimpo = numeroProcesso.replaceAll("\\D", "");
        String siglaTribunal = null;
        if (cnjLimpo.length() >= 16) {
            try {
                siglaTribunal = mapearSiglaTribunalPorNumero(cnjLimpo.substring(14, 16));
            } catch (Exception e) {
                siglaTribunal = null;
            }
        }

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

    /**
     * Overload que aceita SiglaTribunal como entidade JPA.
     * Usa o nome da entidade como fallback quando o código do tribunal
     * não pode ser extraído do CNJ.
     */
    public ProcessoApi buscarDadosOficiais(String numeroProcesso, SiglaTribunal siglaTribunalEntidade) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            return null;
        }

        String cnjLimpo = numeroProcesso.replaceAll("\\D", "");
        String siglaTribunal = null;
        if (cnjLimpo.length() >= 16) {
            try {
                siglaTribunal = mapearSiglaTribunalPorNumero(cnjLimpo.substring(14, 16));
            } catch (Exception e) {
                siglaTribunal = null;
            }
        }

        if (siglaTribunal == null && siglaTribunalEntidade != null) {
            siglaTribunal = siglaTribunalEntidade.getNome();
        }

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

    private String mapearSiglaTribunalPorNumero(String numero) {
        if (numero == null) {
            return null;
        }
        return switch (numero.trim()) {
            case "01" -> "TJAC";
            case "02" -> "TJAL";
            case "03" -> "TJAP";
            case "04" -> "TJAM";
            case "05" -> "TJBA";
            case "06" -> "TJCE";
            case "07" -> "TJDF";
            case "08" -> "TJES";
            case "09" -> "TJGO";
            case "10" -> "TJMA";
            case "11" -> "TJMT";
            case "12" -> "TJMS";
            case "13" -> "TJMG";
            case "14" -> "TJPA";
            case "15" -> "TJPB";
            case "16" -> "TJPR";
            case "17" -> "TJPE";
            case "18" -> "TJPI";
            case "19" -> "TJRJ";
            case "20" -> "TJRN";
            case "21" -> "TJRS";
            case "22" -> "TJRO";
            case "23" -> "TJRR";
            case "24" -> "TJSC";
            case "25" -> "TJSE";
            case "26" -> "TJSP";
            case "27" -> "TJTO";
            default -> throw new TribunalInvalidoException("Código de tribunal inválido.");
        };
    }
}

