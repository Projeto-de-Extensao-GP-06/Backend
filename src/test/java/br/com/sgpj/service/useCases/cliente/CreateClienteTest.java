package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.StatusClienteNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.service.useCases.onedrive.ClienteCaminhoArmazenamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateClienteTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private StatusClienteRepository statusClienteRepository;
    @Mock private ContatoRepository contatoRepository;
    @Mock private ClienteCaminhoArmazenamento clienteCaminhoArmazenamento;
    @Mock private AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos;
    @InjectMocks private CreateCliente createCliente;

    @Test
    void deveRegistrarCaminhoEAtribuirStatusInativoAoClienteSalvoPelaInterface() {
        ClienteCriacaoRequest request = new ClienteCriacaoRequest(
                "Cliente de Teste", "12345678901", true, null,
                ComoConheceuEnum.SITE, null, List.of());
        StatusCliente statusInativo = new StatusCliente();
        statusInativo.setId(2L);
        statusInativo.setNome("INATIVO");
        Cliente clienteSalvo = new Cliente();
        clienteSalvo.setId(10L);
        clienteSalvo.setNomeRazaoSocial(request.nomeRazaoSocial());
        clienteSalvo.setStatusCliente(statusInativo);
        when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.INATIVO.getNome()))
                .thenReturn(Optional.of(statusInativo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);
        when(clienteRepository.findByIdComDetalhes(10L)).thenReturn(Optional.of(clienteSalvo));

        var response = createCliente.criarCliente(request);

        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        var ordem = inOrder(clienteRepository, atualizarStatusClientePorProcessos, clienteCaminhoArmazenamento);
        ordem.verify(clienteRepository).save(clienteCaptor.capture());
        ordem.verify(atualizarStatusClientePorProcessos).sincronizarStatusCliente(clienteSalvo);
        ordem.verify(clienteCaminhoArmazenamento).registrar(clienteSalvo);
        ordem.verify(clienteRepository).findByIdComDetalhes(10L);
        assertEquals("INATIVO", clienteCaptor.getValue().getStatusCliente().getNome());
        assertEquals(10L, response.id());
    }

    @Test
    void deveLancarExcecaoQuandoStatusInativoNaoForEncontrado() {
        ClienteCriacaoRequest request = new ClienteCriacaoRequest(
                "Cliente de Teste", "12345678901", true, null,
                ComoConheceuEnum.SITE, null, List.of());
        when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.INATIVO.getNome()))
                .thenReturn(Optional.empty());

        assertThrows(StatusClienteNaoEncontradoException.class, () -> createCliente.criarCliente(request));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}
