package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.Solicitacao.SolicitacaoCandidatoDuplicidadeResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.dto.response.Processos.ProcessoApiCreateResponse;
import br.com.sgpj.entity.SolicitacaoDuvida;
import br.com.sgpj.entity.Processo.ProcessoApp;

import java.util.List;

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
                solicitacao.getComoConheceu(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getStatus(),
                solicitacao.getPF(),
                Boolean.FALSE.equals(solicitacao.getPF()) ? solicitacao.getNomeCompleto() : null,
                solicitacao.getNomeContato(),
                solicitacao.getCnpj(),
                solicitacao.getUltimaAtualizacao()
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
                solicitacao.getComoConheceu(),
                solicitacao.getAceitouTermos(),
                solicitacao.getDataSolicitacao(),
                solicitacao.getStatus(),
                solicitacao.getDataResposta(),
                processoApiResponse(solicitacao),
                solicitacao.getPF(),
                Boolean.FALSE.equals(solicitacao.getPF()) ? solicitacao.getNomeCompleto() : null,
                solicitacao.getNomeContato(),
                solicitacao.getCnpj(),
                solicitacao.getUltimaAtualizacao()
        );
    }

    public static SolicitacaoClientePreenchimentoResponse toClientePreenchimentoResponse(
            SolicitacaoDuvida solicitacao,
            ProcessoApp processoApp) {
        SolicitacaoClientePreenchimentoResponse preenchimento = toClientePreenchimentoResponse(solicitacao);
        return new SolicitacaoClientePreenchimentoResponse(
                preenchimento.solicitacaoId(),
                preenchimento.clienteId(),
                preenchimento.nomeRazaoSocial(),
                preenchimento.nomeContato(),
                preenchimento.cnpj(),
                preenchimento.email(),
                preenchimento.telefone(),
                preenchimento.estado(),
                preenchimento.numeroProcesso(),
                preenchimento.descricao(),
                preenchimento.comoConheceu(),
                preenchimento.processo(),
                processoApp.getId(),
                processoApp.getCliente().getStatusCliente().getNome(),
                processoApp.getStatus().name()
        );
    }

    public static SolicitacaoClientePreenchimentoResponse toClientePreenchimentoResponse(SolicitacaoDuvida solicitacao) {
        String nomeContato = solicitacao.getNomeContato() != null ? solicitacao.getNomeContato() : solicitacao.getNomeCompleto();
        return new SolicitacaoClientePreenchimentoResponse(
                solicitacao.getId(),
                solicitacao.getClienteId(),
                solicitacao.getNomeCompleto(),
                nomeContato,
                solicitacao.getCnpj(),
                solicitacao.getEmail(),
                solicitacao.getTelefone(),
                solicitacao.getEstado(),
                solicitacao.getNumeroProcesso(),
                solicitacao.getDescricao(),
                solicitacao.getComoConheceu(),
                processoApiResponse(solicitacao),
                solicitacao.getId(),
                null,
                null
        );
    }

    private static ProcessoApiCreateResponse processoApiResponse(SolicitacaoDuvida solicitacao) {
        return ProcessoApiMapper.toCreateResponse(solicitacao.getProcessoApi());
    }
}
