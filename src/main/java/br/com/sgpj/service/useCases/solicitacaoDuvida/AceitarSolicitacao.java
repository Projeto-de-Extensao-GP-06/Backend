package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.ProcessoAppToSolicitacaoDuvidaMapper;
import br.com.sgpj.mapper.SolicitacaoDuvidaMapper;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AceitarSolicitacao {

    private final ProcessoAppRepository processoAppRepository;
    private final ClienteRepository clienteRepository;
    private final StatusClienteRepository statusClienteRepository;

    public AceitarSolicitacao(ProcessoAppRepository processoAppRepository,
                              ClienteRepository clienteRepository,
                              StatusClienteRepository statusClienteRepository) {
        this.processoAppRepository = processoAppRepository;
        this.clienteRepository = clienteRepository;
        this.statusClienteRepository = statusClienteRepository;
    }

    public SolicitacaoClientePreenchimentoResponse aceitar(Long id) {
        ProcessoApp processoApp = buscarProcessoSolicitacao(id);
        Cliente cliente = processoApp.getCliente();
        cliente.setStatusCliente(buscarStatus(StatusClienteEnum.ATIVO));
        cliente.setDataHoraTransformacaoEmCliente(LocalDateTime.now());
        clienteRepository.save(cliente);
        processoApp.setAtivo(true);
        processoApp.setUltimaAtualizacao(LocalDateTime.now());
        processoAppRepository.save(processoApp);

        return SolicitacaoDuvidaMapper.toClientePreenchimentoResponse(
                ProcessoAppToSolicitacaoDuvidaMapper.toSolicitacaoDuvida(processoApp)
        );
    }

    private ProcessoApp buscarProcessoSolicitacao(Long id) {
        return processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Solicitação não encontrada para o ID: " + id));
    }

    private StatusCliente buscarStatus(StatusClienteEnum statusEnum) {
        return statusClienteRepository.findByNomeIgnoreCase(statusEnum.getNome())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Status de cliente não encontrado: " + statusEnum.getNome()));
    }
}
