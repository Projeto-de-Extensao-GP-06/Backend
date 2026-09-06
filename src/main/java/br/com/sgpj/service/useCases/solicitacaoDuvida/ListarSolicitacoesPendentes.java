package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.mapper.ProcessoAppToSolicitacaoDuvidaMapper;
import br.com.sgpj.mapper.SolicitacaoDuvidaMapper;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListarSolicitacoesPendentes {

    private final ProcessoAppRepository processoAppRepository;

    public ListarSolicitacoesPendentes(ProcessoAppRepository processoAppRepository) {
        this.processoAppRepository = processoAppRepository;
    }

    public List<SolicitacaoDuvidaListarResponse> listarPendentes() {
        return processoAppRepository.findByCliente_StatusCliente_NomeIgnoreCaseOrderByDataHoraInicioDesc(StatusClienteEnum.LEAD.getNome())
                .stream()
                .map(ProcessoAppToSolicitacaoDuvidaMapper::toSolicitacaoDuvida)
                .map(SolicitacaoDuvidaMapper::toListarResponse)
                .toList();
    }
}
