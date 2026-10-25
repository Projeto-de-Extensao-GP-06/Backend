package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.mapper.ProcessoAppToSolicitacaoDuvidaMapper;
import br.com.sgpj.mapper.SolicitacaoDuvidaMapper;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ListarSolicitacoesPendentes {

    private final ProcessoAppRepository processoAppRepository;

    public ListarSolicitacoesPendentes(ProcessoAppRepository processoAppRepository) {
        this.processoAppRepository = processoAppRepository;
    }

    public Page<SolicitacaoDuvidaListarResponse> listarPendentes(Pageable pageable) {
        return processoAppRepository.findByCliente_StatusCliente_NomeIgnoreCase(StatusClienteEnum.LEAD.getNome(), pageable)
                .map(ProcessoAppToSolicitacaoDuvidaMapper::toSolicitacaoDuvida)
                .map(SolicitacaoDuvidaMapper::toListarResponse);
    }
}
