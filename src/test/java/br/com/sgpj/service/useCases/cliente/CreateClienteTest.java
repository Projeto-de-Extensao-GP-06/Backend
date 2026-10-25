package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.service.useCases.onedrive.ClienteCaminhoArmazenamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateClienteTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private StatusClienteRepository statusClienteRepository;
    @Mock private ContatoRepository contatoRepository;
    @Mock private ClienteCaminhoArmazenamento clienteCaminhoArmazenamento;
    @InjectMocks private CreateCliente createCliente;

    @Test
    void deveRegistrarCaminhoDoClienteSalvoPelaInterface() {
        ClienteCriacaoRequest request = new ClienteCriacaoRequest(
                "Cliente de Teste", "12345678901", true, null,
                ComoConheceuEnum.SITE, 1L, null, List.of());
        StatusCliente status = new StatusCliente();
        status.setId(1L);
        status.setNome("ATIVO");
        Cliente clienteSalvo = new Cliente();
        clienteSalvo.setId(10L);
        clienteSalvo.setNomeRazaoSocial(request.nomeRazaoSocial());
        when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(status));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);
        when(clienteRepository.findByIdComDetalhes(10L)).thenReturn(Optional.of(clienteSalvo));

        var response = createCliente.criarCliente(request);

        var ordem = inOrder(clienteRepository, clienteCaminhoArmazenamento);
        ordem.verify(clienteRepository).save(any(Cliente.class));
        ordem.verify(clienteCaminhoArmazenamento).registrar(clienteSalvo);
        ordem.verify(clienteRepository).findByIdComDetalhes(10L);
        assertEquals(10L, response.id());
    }
}
