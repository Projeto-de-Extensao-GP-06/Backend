package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.SiglaTribunalResponse;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.exception.TribunalInvalidoException;

public class SiglaTribunalMapper {

    public static SiglaTribunalResponse toResponse(SiglaTribunal siglaTribunal){

        return new SiglaTribunalResponse(siglaTribunal.getId(), siglaTribunal.getNome());
    }

    public static String mapearSiglaTribunalPorNumero(String numero) {
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
