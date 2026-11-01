package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AtualizarStatusClientePorProcessos {

    private final ClienteRepository clienteRepository;
    private final ProcessoAppRepository processoAppRepository;
    private final StatusClienteRepository statusClienteRepository;

    public AtualizarStatusClientePorProcessos(
            ClienteRepository clienteRepository,
            ProcessoAppRepository processoAppRepository,
            StatusClienteRepository statusClienteRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.processoAppRepository = processoAppRepository;
        this.statusClienteRepository = statusClienteRepository;
    }

    /**
     * Define o status do cliente como ATIVO (usado na criação ou reativação de processo para ATIVO).
     */
    @Transactional
    public void ativarCliente(Cliente cliente) {
        if (cliente == null) {
            return;
        }
        aplicarStatus(cliente, true);
    }

    /**
     * Analisa e atualiza o status do cliente após a alteração (para não-ATIVO) ou exclusão de um processo,
     * ignorando o processo especificado (idProcessoIgnorado).
     */
    @Transactional
    public void atualizarStatusAposAlteracaoOuExclusao(Cliente cliente, Long idProcessoIgnorado) {
        if (cliente == null || cliente.getId() == null) {
            return;
        }
        boolean possuiOutroProcessoAtivo = processoAppRepository.existsByCliente_IdAndStatusAndIdNot(
                cliente.getId(), StatusProcesso.ATIVO, idProcessoIgnorado);
        aplicarStatus(cliente, possuiOutroProcessoAtivo);
    }

    /**
     * Sincroniza o status avaliando todos os processos persistidos do cliente.
     */
    @Transactional
    public void sincronizarStatusCliente(Cliente cliente) {
        if (cliente == null || cliente.getId() == null) {
            return;
        }
        boolean possuiProcessoAtivo = processoAppRepository.existsByCliente_IdAndStatus(
                cliente.getId(), StatusProcesso.ATIVO);
        aplicarStatus(cliente, possuiProcessoAtivo);
    }

    private void aplicarStatus(Cliente cliente, boolean deveSerAtivo) {
        String statusDesejado = deveSerAtivo ? StatusClienteEnum.ATIVO.getNome() : StatusClienteEnum.INATIVO.getNome();

        StatusCliente statusAtual = cliente.getStatusCliente();
        if (statusAtual != null && statusDesejado.equalsIgnoreCase(statusAtual.getNome())) {
            return;
        }

        StatusCliente novoStatus = statusClienteRepository.findByNomeIgnoreCase(statusDesejado)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Status do cliente não encontrado: " + statusDesejado));

        cliente.setStatusCliente(novoStatus);
        if (deveSerAtivo && cliente.getDataHoraTransformacaoEmCliente() == null) {
            cliente.setDataHoraTransformacaoEmCliente(LocalDateTime.now());
        }
        clienteRepository.save(cliente);
    }
}
