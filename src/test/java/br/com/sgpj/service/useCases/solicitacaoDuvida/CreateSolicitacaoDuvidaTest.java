package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.service.useCases.processo.ProcessoApiGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSolicitacaoDuvida Use Case")
class CreateSolicitacaoDuvidaTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private StatusClienteRepository statusClienteRepository;

    @Mock
    private ProcessoApiGateway processoApiGateway;

    @InjectMocks
    private CreateSolicitacaoDuvida createSolicitacaoDuvida;

    private StatusCliente statusLead;

    @BeforeEach
    void setUp() {
        statusLead = new StatusCliente();
        statusLead.setId(1L);
        statusLead.setNome("LEAD");
    }

    @Nested
    @DisplayName("criarSolicitacao")
    class CriarSolicitacaoTest {

        @Test
        @DisplayName("Deve criar solicitação com status SOLICITACAO no processoApp e LEAD no cliente")
        void deveCriarSolicitacaoComStatusSolicitacao() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Fulano de Tal",
                    "fulano@email.com",
                    true,
                    "11999999999",
                    "SP",
                    null,
                    "Descrição da dúvida",
                    true
            );

            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.LEAD.getNome()))
                    .thenReturn(Optional.of(statusLead));
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
                Cliente c = inv.getArgument(0);
                c.setId(10L);
                return c;
            });
            when(contatoRepository.save(any(Contato.class))).thenAnswer(inv -> {
                Contato c = inv.getArgument(0);
                c.setId(20L);
                return c;
            });
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> {
                ProcessoApp p = inv.getArgument(0);
                p.setId(30L);
                return p;
            });

            SolicitacaoDuvidaDetalhesResponse response = createSolicitacaoDuvida.criarSolicitacao(request);

            ArgumentCaptor<ProcessoApp> processoCaptor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(processoCaptor.capture());

            ProcessoApp salvo = processoCaptor.getValue();
            assertEquals(StatusProcesso.SOLICITACAO, salvo.getStatus());
            assertEquals("Descrição da dúvida", salvo.getDescricaoCaso());
            assertEquals("Solicitação enviada pelo formulário público. Estado: SP", salvo.getObservacao());
            assertNotNull(response);
            verify(processoApiGateway, never()).buscarDadosOficiais(any(), any());
        }

        @Test
        @DisplayName("Deve buscar processo oficial quando numeroProcesso for informado")
        void deveBuscarProcessoOficialQuandoNumeroProcessoInformado() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Fulano de Tal",
                    "fulano@email.com",
                    true,
                    "11999999999",
                    "SP",
                    "0000001-00.2026.8.26.0001",
                    "Descrição da dúvida",
                    true
            );

            ProcessoApi apiMock = new ProcessoApi();
            apiMock.setNumeroCnj("0000001-00.2026.8.26.0001");

            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.LEAD.getNome()))
                    .thenReturn(Optional.of(statusLead));
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
                Cliente c = inv.getArgument(0);
                c.setId(10L);
                return c;
            });
            when(contatoRepository.save(any(Contato.class))).thenAnswer(inv -> {
                Contato c = inv.getArgument(0);
                c.setId(20L);
                return c;
            });
            when(processoApiGateway.buscarDadosOficiais(any(), any())).thenReturn(apiMock);
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> {
                ProcessoApp p = inv.getArgument(0);
                p.setId(30L);
                return p;
            });

            SolicitacaoDuvidaDetalhesResponse response = createSolicitacaoDuvida.criarSolicitacao(request);

            ArgumentCaptor<ProcessoApp> processoCaptor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(processoCaptor.capture());

            assertEquals(apiMock, processoCaptor.getValue().getProcessoApi());
            assertEquals(StatusProcesso.SOLICITACAO, processoCaptor.getValue().getStatus());
            assertNotNull(response);
        }

        @Test
        @DisplayName("Deve tratar falha na busca de processo oficial sem interromper a criação")
        void deveTratarFalhaNaBuscaDeProcessoOficial() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Fulano de Tal",
                    "fulano@email.com",
                    true,
                    "11999999999",
                    "SP",
                    "0000001-00.2026.8.26.0001",
                    "Descrição da dúvida",
                    true
            );

            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.LEAD.getNome()))
                    .thenReturn(Optional.of(statusLead));
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
                Cliente c = inv.getArgument(0);
                c.setId(10L);
                return c;
            });
            when(contatoRepository.save(any(Contato.class))).thenAnswer(inv -> {
                Contato c = inv.getArgument(0);
                c.setId(20L);
                return c;
            });
            when(processoApiGateway.buscarDadosOficiais(any(), any()))
                    .thenThrow(new RuntimeException("API indisponível"));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> {
                ProcessoApp p = inv.getArgument(0);
                p.setId(30L);
                return p;
            });

            SolicitacaoDuvidaDetalhesResponse response = createSolicitacaoDuvida.criarSolicitacao(request);

            assertNotNull(response);
            verify(processoAppRepository).save(any(ProcessoApp.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando status LEAD não for encontrado")
        void deveLancarExcecaoQuandoStatusLeadNaoEncontrado() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Fulano de Tal",
                    "fulano@email.com",
                    true,
                    "11999999999",
                    "SP",
                    null,
                    "Descrição",
                    true
            );

            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.LEAD.getNome()))
                    .thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> createSolicitacaoDuvida.criarSolicitacao(request));
            verify(clienteRepository, never()).save(any());
            verify(processoAppRepository, never()).save(any());
        }
    }
}
