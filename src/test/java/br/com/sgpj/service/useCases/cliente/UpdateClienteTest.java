package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateClienteTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private ContatoRepository contatoRepository;
    @Mock private StatusClienteRepository statusClienteRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos;
    @InjectMocks private UpdateCliente updateCliente;

    @Test
    void deveSincronizarStatusComProcessosAposAtualizarCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setCpfCnpj("12345678901");
        StatusCliente status = new StatusCliente(1L, "ATIVO");
        ClienteAtualizacaoRequest request = new ClienteAtualizacaoRequest(
                "Cliente de Teste",
                "12345678901",
                null,
                ComoConheceuEnum.SITE,
                1L,
                null,
                List.of());

        when(clienteRepository.findByIdComDetalhes(10L)).thenReturn(Optional.of(cliente));
        when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(status));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        updateCliente.atualizarCliente(10L, request);

        verify(atualizarStatusClientePorProcessos).sincronizarStatusCliente(cliente);
    }
}
