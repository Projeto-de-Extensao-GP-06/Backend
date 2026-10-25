package br.com.sgpj.mapper;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.SolicitacaoDuvida;
import br.com.sgpj.entity.SolicitacaoStatus;
import br.com.sgpj.entity.StatusClienteEnum;

public class ProcessoAppToSolicitacaoDuvidaMapper {

    private static final String OBSERVACAO_SOLICITACAO_PREFIXO = "Solicitação enviada pelo formulário público. Estado: ";

    private ProcessoAppToSolicitacaoDuvidaMapper() {
    }

    public static SolicitacaoDuvida toSolicitacaoDuvida(ProcessoApp processoApp) {
        Cliente cliente = processoApp.getCliente();
        Contato contato = contatoPrincipal(cliente);
        ProcessoApi processoApi = processoApp.getProcessoApi();

        SolicitacaoDuvida solicitacao = new SolicitacaoDuvida();
        solicitacao.setId(processoApp.getId());
        solicitacao.setClienteId(cliente.getId());
        solicitacao.setPF(cliente.getPF());
        solicitacao.setNomeCompleto(cliente.getNomeRazaoSocial());
        solicitacao.setNomeContato(contato != null ? contato.getNome() : null);
        solicitacao.setCnpj(processoApp.getCnpjInformadoFormulario());
        solicitacao.setEmail(contato != null ? contato.getEmail() : null);
        solicitacao.setTelefone(contato != null ? contato.getTelefone() : null);
        solicitacao.setEstado(extrairEstado(processoApp.getObservacao()));
        solicitacao.setNumeroProcesso(processoApi != null ? processoApi.getNumeroCnj() : null);
        solicitacao.setDescricao(processoApp.getDescricaoCaso());
        solicitacao.setComoConheceu(cliente.getComoConheceu());
        solicitacao.setAceitouTermos(true);
        solicitacao.setDataSolicitacao(processoApp.getDataHoraInicio());
        solicitacao.setDataResposta(cliente.getDataHoraTransformacaoEmCliente());
        solicitacao.setStatus(toSolicitacaoStatus(cliente));
        solicitacao.setProcessoApi(processoApi);
        return solicitacao;
    }

    private static Contato contatoPrincipal(Cliente cliente) {
        if (cliente.getContatos() == null || cliente.getContatos().isEmpty()) {
            return null;
        }

        return cliente.getContatos().stream()
                .filter(contato -> Boolean.TRUE.equals(contato.getPrincipal()))
                .findFirst()
                .orElse(cliente.getContatos().getFirst());
    }

    private static SolicitacaoStatus toSolicitacaoStatus(Cliente cliente) {
        String status = cliente.getStatusCliente() != null ? cliente.getStatusCliente().getNome() : null;
        if (StatusClienteEnum.ATIVO.getNome().equalsIgnoreCase(status)) {
            return SolicitacaoStatus.ACEITA;
        }
        if (StatusClienteEnum.INATIVO.getNome().equalsIgnoreCase(status)) {
            return SolicitacaoStatus.NEGADA;
        }
        return SolicitacaoStatus.PENDENTE;
    }

    private static String extrairEstado(String observacao) {
        if (observacao == null || !observacao.startsWith(OBSERVACAO_SOLICITACAO_PREFIXO)) {
            return null;
        }
        return observacao.substring(OBSERVACAO_SOLICITACAO_PREFIXO.length());
    }
}
