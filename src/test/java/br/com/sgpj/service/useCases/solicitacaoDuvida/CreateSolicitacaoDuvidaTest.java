package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.ComoConheceuEnum;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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
                    null,
                    "Descrição da dúvida",
                    ComoConheceuEnum.INSTAGRAM,
                    true,
                    null,
                    null,
                    "SP",
                    "dummy-test-token"
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
            verify(processoApiGateway, never()).buscarDadosOficiais(any());
        }

        @Test
        @DisplayName("Deve criar solicitação PJ gravando razão social no cliente, nome do solicitante no contato e CNPJ informado no ProcessoApp sem tocar no cpf_cnpj do cliente")
        void deveCriarSolicitacaoPjComRazaoSocialContatoECnpjInformadoSemTocarCpfCnpj() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Representante",
                    "carlos@techcorp.com",
                    false,
                    "11988887777",
                    null,
                    "Assessoria jurídica para empresa",
                    ComoConheceuEnum.SITE,
                    true,
                    "TechCorp Soluções LTDA",
                    "12345678000195"
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

            createSolicitacaoDuvida.criarSolicitacao(request);

            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(clienteCaptor.capture());
            Cliente clienteSalvo = clienteCaptor.getValue();

            ArgumentCaptor<Contato> contatoCaptor = ArgumentCaptor.forClass(Contato.class);
            verify(contatoRepository).save(contatoCaptor.capture());
            Contato contatoSalvo = contatoCaptor.getValue();

            ArgumentCaptor<ProcessoApp> processoCaptor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(processoCaptor.capture());
            ProcessoApp processoSalvo = processoCaptor.getValue();

            // Cliente deve conter Razão Social e NÃO deve conter CPF/CNPJ definitivo
            assertEquals("TechCorp Soluções LTDA", clienteSalvo.getNomeRazaoSocial());
            assertEquals(false, clienteSalvo.getPF());
            assertNull(clienteSalvo.getCpfCnpj(), "cpf_cnpj do Cliente NÃO deve ser preenchido no fluxo público de solicitação");

            // Contato deve conter o nome do solicitante
            assertEquals("Carlos Representante", contatoSalvo.getNome());
            assertEquals("carlos@techcorp.com", contatoSalvo.getEmail());

            // ProcessoApp deve conter o CNPJ informado
            assertEquals("12345678000195", processoSalvo.getCnpjInformadoFormulario());
        }

        @Test
        @DisplayName("Deve criar solicitação PF com cnpjInformado nulo mesmo se campos de PJ forem passados")
        void deveCriarSolicitacaoPfIgnorandoRazaoSocialECnpj() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Ana Beatriz",
                    "ana@email.com",
                    true,
                    "11977776666",
                    null,
                    "Dúvida trabalhista",
                    ComoConheceuEnum.INDICACAO,
                    true,
                    "Razão Ignorada LTDA",
                    "12345678000195"
            );

            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.LEAD.getNome()))
                    .thenReturn(Optional.of(statusLead));
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
                Cliente c = inv.getArgument(0);
                c.setId(11L);
                return c;
            });
            when(contatoRepository.save(any(Contato.class))).thenAnswer(inv -> {
                Contato c = inv.getArgument(0);
                c.setId(21L);
                return c;
            });
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> {
                ProcessoApp p = inv.getArgument(0);
                p.setId(31L);
                return p;
            });

            createSolicitacaoDuvida.criarSolicitacao(request);

            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(clienteCaptor.capture());
            Cliente clienteSalvo = clienteCaptor.getValue();

            ArgumentCaptor<ProcessoApp> processoCaptor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(processoCaptor.capture());
            ProcessoApp processoSalvo = processoCaptor.getValue();

            assertEquals("Ana Beatriz", clienteSalvo.getNomeRazaoSocial());
            assertEquals(true, clienteSalvo.getPF());
            assertNull(clienteSalvo.getCpfCnpj());
            assertNull(processoSalvo.getCnpjInformadoFormulario(), "CNPJ informado deve ser nulo para PF");
        }

        @Test
        @DisplayName("Deve buscar processo oficial quando numeroProcesso for informado")
        void deveBuscarProcessoOficialQuandoNumeroProcessoInformado() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Fulano de Tal",
                    "fulano@email.com",
                    true,
                    "11999999999",
                    "0000001-00.2026.8.26.0001",
                    "Descrição da dúvida",
                    ComoConheceuEnum.INSTAGRAM,
                    true,
                    null,
                    null
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
            when(processoApiGateway.buscarDadosOficiais(any())).thenReturn(apiMock);
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
                    "0000001-00.2026.8.26.0001",
                    "Descrição da dúvida",
                    ComoConheceuEnum.INSTAGRAM,
                    true,
                    null,
                    null
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
            when(processoApiGateway.buscarDadosOficiais(any()))
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
                    null,
                    "Descrição",
                    ComoConheceuEnum.INSTAGRAM,
                    true,
                    null,
                    null
            );

            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.LEAD.getNome()))
                    .thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> createSolicitacaoDuvida.criarSolicitacao(request));
            verify(clienteRepository, never()).save(any());
            verify(processoAppRepository, never()).save(any());
        }
    }
}
