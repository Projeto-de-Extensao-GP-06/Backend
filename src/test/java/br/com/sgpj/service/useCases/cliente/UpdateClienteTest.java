package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateClienteTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private ContatoRepository contatoRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos;
    @InjectMocks private UpdateCliente updateCliente;

    @Test
    void deveSincronizarStatusComProcessosAposAtualizarCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setCpfCnpj("12345678901");
        StatusCliente status = new StatusCliente(3L, "ATIVO");
        cliente.setStatusCliente(status);
        ClienteAtualizacaoRequest request = new ClienteAtualizacaoRequest(
                "Cliente de Teste",
                "12345678901",
                null,
                ComoConheceuEnum.SITE,
                null,
                List.of());

        when(clienteRepository.findByIdComDetalhes(10L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        updateCliente.atualizarCliente(10L, request);

        assertEquals(status, cliente.getStatusCliente());
        verify(atualizarStatusClientePorProcessos).sincronizarStatusCliente(cliente);
    }
}
