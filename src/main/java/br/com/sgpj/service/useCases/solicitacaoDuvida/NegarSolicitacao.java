package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.service.useCases.processo.DeleteProcessoApp;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class NegarSolicitacao {

    private final ProcessoAppRepository processoAppRepository;
    private final ClienteRepository clienteRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final DeleteProcessoApp deleteProcessoApp;

    public NegarSolicitacao(ProcessoAppRepository processoAppRepository,
                            ClienteRepository clienteRepository,
                            StatusClienteRepository statusClienteRepository,
                            DeleteProcessoApp deleteProcessoApp) {
        this.processoAppRepository = processoAppRepository;
        this.clienteRepository = clienteRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.deleteProcessoApp = deleteProcessoApp;
    }

    @Transactional
    public void negar(Long id) {
        ProcessoApp processoApp = buscarProcessoSolicitacao(id);
        Cliente cliente = processoApp.getCliente();
        boolean clienteAtivo = cliente.getStatusCliente() != null
                && StatusClienteEnum.ATIVO.getNome().equalsIgnoreCase(cliente.getStatusCliente().getNome());
        boolean possuiOutroProcesso = processoAppRepository.existsByCliente_IdAndIdNot(cliente.getId(), id);

        if (!clienteAtivo || !possuiOutroProcesso) {
            cliente.setStatusCliente(buscarStatus(StatusClienteEnum.INATIVO));
            cliente.setDataHoraTransformacaoEmCliente(LocalDateTime.now());
            clienteRepository.save(cliente);
        }

        if (processoApp.getProcessoApi() != null
                && processoAppRepository.existsByProcessoApi_IdAndIdNot(processoApp.getProcessoApi().getId(), id)) {
            processoApp.setProcessoApi(null);
        }

        deleteProcessoApp.deletarProcesso(id);
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
