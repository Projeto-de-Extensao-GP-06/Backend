package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.mapper.ProcessoAppToSolicitacaoDuvidaMapper;
import br.com.sgpj.mapper.SolicitacaoDuvidaMapper;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FiltrarSolicitacoes {

    private static final String OBSERVACAO_SOLICITACAO_PREFIXO =
            "Solicitação enviada pelo formulário público.";

    private final ProcessoAppRepository processoAppRepository;

    public FiltrarSolicitacoes(ProcessoAppRepository processoAppRepository) {
        this.processoAppRepository = processoAppRepository;
    }

    public Page<SolicitacaoDuvidaListarResponse> filtrar(
            String estado,
            Boolean isPF,
            String pesquisa,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable) {
        return processoAppRepository.buscarSolicitacoesFiltradas(
                        normalizar(estado),
                        isPF,
                        OBSERVACAO_SOLICITACAO_PREFIXO,
                        normalizar(pesquisa),
                        dataInicio,
                        dataFim,
                        pageable)
                .map(ProcessoAppToSolicitacaoDuvidaMapper::toSolicitacaoDuvida)
                .map(SolicitacaoDuvidaMapper::toListarResponse);
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
