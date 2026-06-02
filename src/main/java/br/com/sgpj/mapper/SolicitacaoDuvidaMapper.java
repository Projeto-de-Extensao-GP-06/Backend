package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.dto.response.Processos.ProcessoApiCreateResponse;
import br.com.sgpj.entity.SolicitacaoDuvida;

public class SolicitacaoDuvidaMapper {

    private SolicitacaoDuvidaMapper() {
    }

    public static SolicitacaoDuvidaListarResponse toListarResponse(SolicitacaoDuvida solicitacao) {
        return new SolicitacaoDuvidaListarResponse(
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getNomeCompleto(),
                solicitacao.getEmail(),
                solicitacao.getTelefone(),
                solicitacao.getEstado(),
                solicitacao.getNumeroProcesso(),
                solicitacao.getDescricao(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getStatus()
        );
    }

    public static SolicitacaoDuvidaDetalhesResponse toDetalhesResponse(SolicitacaoDuvida solicitacao) {
        return new SolicitacaoDuvidaDetalhesResponse(
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getNomeCompleto(),
                solicitacao.getEmail(),
                solicitacao.getTelefone(),
                solicitacao.getEstado(),
                solicitacao.getNumeroProcesso(),
                solicitacao.getDescricao(),
                solicitacao.getAceitouTermos(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getStatus(),
                solicitacao.getDataResposta(),
                processoApiResponse(solicitacao)
        );
    }

    public static SolicitacaoClientePreenchimentoResponse toClientePreenchimentoResponse(SolicitacaoDuvida solicitacao) {
        return new SolicitacaoClientePreenchimentoResponse(
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getNomeCompleto(),
                solicitacao.getNomeCompleto(),
                solicitacao.getEmail(),
                solicitacao.getTelefone(),
                solicitacao.getEstado(),
                solicitacao.getNumeroProcesso(),
                solicitacao.getDescricao(),
                processoApiResponse(solicitacao)
        );
    }

    private static ProcessoApiCreateResponse processoApiResponse(SolicitacaoDuvida solicitacao) {
        return ProcessoApiMapper.toCreateResponse(solicitacao.getProcessoApi());
    }
}
