package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.Solicitacao.MotivoDuplicidadeEnum;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoCandidatoDuplicidadeResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarCandidatosDuplicidadeSolicitacao Use Case")
class BuscarCandidatosDuplicidadeSolicitacaoTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContatoRepository contatoRepository;

    @InjectMocks
    private BuscarCandidatosDuplicidadeSolicitacao useCase;

    private StatusCliente statusAtivo;
    private StatusCliente statusLead;

    @BeforeEach
    void setUp() {
        statusAtivo = new StatusCliente();
        statusAtivo.setId(1L);
        statusAtivo.setNome("ATIVO");

        statusLead = new StatusCliente();
        statusLead.setId(2L);
        statusLead.setNome("LEAD");
    }

    @Nested
    @DisplayName("Cenários de Pessoa Jurídica (PJ)")
    class CenariosPj {

        @Test
        @DisplayName("Deve encontrar candidato por CNPJ igual")
        void deveEncontrarCandidatoPorCnpj() {
            Cliente clienteSolicitacao = new Cliente();
            clienteSolicitacao.setId(10L);
            clienteSolicitacao.setPF(false);
            clienteSolicitacao.setNomeRazaoSocial("Empresa Nova LTDA");

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setId(100L);
            processoApp.setCliente(clienteSolicitacao);
            processoApp.setCnpjInformadoFormulario("12.345.678/0001-95");

            Cliente clienteExistente = new Cliente();
            clienteExistente.setId(20L);
            clienteExistente.setPF(false);
            clienteExistente.setNomeRazaoSocial("Empresa Existente LTDA");
            clienteExistente.setStatusCliente(statusAtivo);
            clienteExistente.setDataHoraEnvioFormulario(LocalDateTime.now().minusDays(10));

            when(clienteRepository.findCandidatosPorCpfCnpj("12345678000195", 10L))
                    .thenReturn(List.of(clienteExistente));

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarCandidatos(processoApp);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());

            SolicitacaoCandidatoDuplicidadeResponse candidato = resultado.getFirst();
            assertEquals(20L, candidato.clienteId());
            assertEquals("Empresa Existente LTDA", candidato.nomeRazaoSocial());
            assertEquals("ATIVO", candidato.statusCliente());
            assertTrue(candidato.motivos().contains(MotivoDuplicidadeEnum.CNPJ));

            verify(clienteRepository).findCandidatosPorCpfCnpj("12345678000195", 10L);
            verifyNoInteractions(contatoRepository);
        }

        @Test
        @DisplayName("Não deve buscar duplicidade se CNPJ informado for nulo ou em branco para PJ")
        void naoDeveBuscarSeCnpjEmBranco() {
            Cliente clienteSolicitacao = new Cliente();
            clienteSolicitacao.setId(10L);
            clienteSolicitacao.setPF(false);

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setCliente(clienteSolicitacao);
            processoApp.setCnpjInformadoFormulario("   ");

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarCandidatos(processoApp);

            assertTrue(resultado.isEmpty());
            verifyNoInteractions(clienteRepository);
            verifyNoInteractions(contatoRepository);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum candidato PJ for encontrado por CNPJ")
        void deveRetornarListaVaziaQuandoNenhumCandidatoPjForEncontrado() {
            Cliente clienteSolicitacao = new Cliente();
            clienteSolicitacao.setId(10L);
            clienteSolicitacao.setPF(false);

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setCliente(clienteSolicitacao);
            processoApp.setCnpjInformadoFormulario("12345678000195");

            when(clienteRepository.findCandidatosPorCpfCnpj("12345678000195", 10L))
                    .thenReturn(List.of());

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarCandidatos(processoApp);

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(clienteRepository).findCandidatosPorCpfCnpj("12345678000195", 10L);
        }
    }

    @Nested
    @DisplayName("Cenários de Pessoa Física (PF)")
    class CenariosPf {

        @Test
        @DisplayName("Deve encontrar candidato por e-mail, telefone e nome, consolidando no mesmo cliente")
        void deveConsolidarMultiplosSinaisNoMesmoCliente() {
            Cliente clienteSolicitacao = new Cliente();
            clienteSolicitacao.setId(10L);
            clienteSolicitacao.setPF(true);
            clienteSolicitacao.setNomeRazaoSocial("João Silva");

            Contato contatoSolicitacao = new Contato();
            contatoSolicitacao.setEmail("joao@email.com");
            contatoSolicitacao.setTelefone("(11) 98888-7777");
            contatoSolicitacao.setPrincipal(true);
            clienteSolicitacao.setContatos(List.of(contatoSolicitacao));

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setCliente(clienteSolicitacao);

            Cliente clienteExistente = new Cliente();
            clienteExistente.setId(50L);
            clienteExistente.setPF(true);
            clienteExistente.setNomeRazaoSocial("João Silva");
            clienteExistente.setStatusCliente(statusLead);

            Contato contatoExistente = new Contato();
            contatoExistente.setId(501L);
            contatoExistente.setEmail("joao@email.com");
            contatoExistente.setTelefone("11988887777");
            contatoExistente.setCliente(clienteExistente);

            when(contatoRepository.findContatosPorEmail("joao@email.com", 10L))
                    .thenReturn(List.of(contatoExistente));
            when(contatoRepository.findContatosPorTelefoneNormalizado("11988887777", 10L))
                    .thenReturn(List.of(contatoExistente));
            when(clienteRepository.findCandidatosPorNome("João Silva", 10L))
                    .thenReturn(List.of(clienteExistente));

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarCandidatos(processoApp);

            assertEquals(1, resultado.size());
            SolicitacaoCandidatoDuplicidadeResponse candidato = resultado.getFirst();
            assertEquals(50L, candidato.clienteId());
            assertEquals(3, candidato.motivos().size());
            assertTrue(candidato.motivos().contains(MotivoDuplicidadeEnum.EMAIL));
            assertTrue(candidato.motivos().contains(MotivoDuplicidadeEnum.TELEFONE));
            assertTrue(candidato.motivos().contains(MotivoDuplicidadeEnum.NOME));
            assertTrue(candidato.contatosCorrespondentes().contains("joao@email.com"));
            assertTrue(candidato.contatosCorrespondentes().contains("11988887777"));
        }

        @Test
        @DisplayName("Deve ordenar candidatos com mais sinais primeiro")
        void deveOrdenarPorMaisSinais() {
            Cliente clienteSolicitacao = new Cliente();
            clienteSolicitacao.setId(1L);
            clienteSolicitacao.setPF(true);
            clienteSolicitacao.setNomeRazaoSocial("Maria Souza");

            Contato contatoSolicitacao = new Contato();
            contatoSolicitacao.setEmail("maria@email.com");
            contatoSolicitacao.setTelefone("11999990000");
            contatoSolicitacao.setPrincipal(true);
            clienteSolicitacao.setContatos(List.of(contatoSolicitacao));

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setCliente(clienteSolicitacao);

            // Cliente A: bate apenas nome
            Cliente clienteA = new Cliente();
            clienteA.setId(100L);
            clienteA.setNomeRazaoSocial("Maria Souza");
            clienteA.setStatusCliente(statusAtivo);

            // Cliente B: bate email e telefone (2 sinais)
            Cliente clienteB = new Cliente();
            clienteB.setId(200L);
            clienteB.setNomeRazaoSocial("Outro Nome");
            clienteB.setStatusCliente(statusLead);

            Contato contatoB = new Contato();
            contatoB.setCliente(clienteB);
            contatoB.setEmail("maria@email.com");
            contatoB.setTelefone("11999990000");

            when(contatoRepository.findContatosPorEmail("maria@email.com", 1L)).thenReturn(List.of(contatoB));
            when(contatoRepository.findContatosPorTelefoneNormalizado("11999990000", 1L)).thenReturn(List.of(contatoB));
            when(clienteRepository.findCandidatosPorNome("Maria Souza", 1L)).thenReturn(List.of(clienteA));

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarCandidatos(processoApp);

            assertEquals(1, resultado.size(), "Correspondência apenas pelo nome não deve ser tratada como duplicidade");
            assertEquals(200L, resultado.getFirst().clienteId());
            assertEquals(2, resultado.getFirst().motivos().size());
        }

        @Test
        @DisplayName("Deve limitar resultado em no máximo 10 candidatos ordenados por relevância")
        void deveLimitarResultadoEmNoMaximo10Candidatos() {
            Cliente clienteSolicitacao = new Cliente();
            clienteSolicitacao.setId(1L);
            clienteSolicitacao.setPF(true);
            clienteSolicitacao.setNomeRazaoSocial("Nome Comum");

            Contato contatoSolicitacao = new Contato();
            contatoSolicitacao.setEmail("comum@email.com");
            contatoSolicitacao.setPrincipal(true);
            clienteSolicitacao.setContatos(List.of(contatoSolicitacao));

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setCliente(clienteSolicitacao);

            // Criar 15 candidatos com o mesmo e-mail
            java.util.List<Contato> quinzeContatos = new java.util.ArrayList<>();
            for (long i = 101L; i <= 115L; i++) {
                Cliente c = new Cliente();
                c.setId(i);
                c.setNomeRazaoSocial("Cliente " + i);
                c.setStatusCliente(statusAtivo);

                Contato contato = new Contato();
                contato.setCliente(c);
                contato.setEmail("comum@email.com");
                quinzeContatos.add(contato);
            }

            when(contatoRepository.findContatosPorEmail("comum@email.com", 1L)).thenReturn(quinzeContatos);

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarCandidatos(processoApp);

            assertNotNull(resultado);
            assertEquals(10, resultado.size(), "Deve conter no máximo 10 candidatos retornados");
        }

        @Test
        @DisplayName("Não deve tratar correspondência apenas pelo nome como duplicidade")
        void naoDeveRetornarCandidatoQueCoincideApenasPeloNome() {
            Cliente clienteSolicitacao = new Cliente();
            clienteSolicitacao.setId(1L);
            clienteSolicitacao.setPF(true);
            clienteSolicitacao.setNomeRazaoSocial("Maria Souza");

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setCliente(clienteSolicitacao);

            Cliente clienteMesmoNome = new Cliente();
            clienteMesmoNome.setId(2L);
            clienteMesmoNome.setNomeRazaoSocial("Maria Souza");

            when(clienteRepository.findCandidatosPorNome("Maria Souza", 1L))
                    .thenReturn(List.of(clienteMesmoNome));

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarCandidatos(processoApp);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve exibir status correto para candidato INATIVO e null quando status for nulo")
        void deveTratarStatusNuloEExibirStatusCorreto() {
            Cliente clienteSolicitacao = new Cliente();
            clienteSolicitacao.setId(1L);
            clienteSolicitacao.setPF(true);
            clienteSolicitacao.setNomeRazaoSocial("Teste Status");

            Contato contatoSolicitacao = new Contato();
            contatoSolicitacao.setEmail("teste@email.com");
            contatoSolicitacao.setPrincipal(true);
            clienteSolicitacao.setContatos(List.of(contatoSolicitacao));

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setCliente(clienteSolicitacao);

            StatusCliente statusInativo = new StatusCliente();
            statusInativo.setNome("INATIVO");

            Cliente clienteInativo = new Cliente();
            clienteInativo.setId(500L);
            clienteInativo.setNomeRazaoSocial("Teste Status");
            clienteInativo.setStatusCliente(statusInativo);

            Cliente clienteSemStatus = new Cliente();
            clienteSemStatus.setId(501L);
            clienteSemStatus.setNomeRazaoSocial("Teste Status");
            clienteSemStatus.setStatusCliente(null);

            Contato contatoInativo = new Contato();
            contatoInativo.setCliente(clienteInativo);
            contatoInativo.setEmail("teste@email.com");

            Contato contatoSemStatus = new Contato();
            contatoSemStatus.setCliente(clienteSemStatus);
            contatoSemStatus.setEmail("teste@email.com");

            when(contatoRepository.findContatosPorEmail("teste@email.com", 1L))
                    .thenReturn(List.of(contatoInativo, contatoSemStatus));

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarCandidatos(processoApp);

            assertEquals(2, resultado.size());
            assertEquals("INATIVO", resultado.get(0).statusCliente());
            assertNull(resultado.get(1).statusCliente());
        }

        @Test
        @DisplayName("Deve retornar lista vazia se processoApp ou cliente forem nulos")
        void deveRetornarVazioComParametrosNulos() {
            assertTrue(useCase.buscarCandidatos(null).isEmpty());

            ProcessoApp appSemCliente = new ProcessoApp();
            assertTrue(useCase.buscarCandidatos(appSemCliente).isEmpty());
        }
    }

    @Nested
    @DisplayName("buscarPorSolicitacaoId")
    class BuscarPorSolicitacaoIdTest {

        @Test
        @DisplayName("Deve buscar processo por ID e retornar candidatos")
        void deveBuscarPorSolicitacaoIdComSucesso() {
            Cliente cliente = new Cliente();
            cliente.setId(10L);
            cliente.setPF(false);

            ProcessoApp processoApp = new ProcessoApp();
            processoApp.setId(5L);
            processoApp.setCliente(cliente);
            processoApp.setCnpjInformadoFormulario("12345678000195");

            when(processoAppRepository.findById(5L)).thenReturn(java.util.Optional.of(processoApp));
            when(clienteRepository.findCandidatosPorCpfCnpj("12345678000195", 10L)).thenReturn(List.of());

            List<SolicitacaoCandidatoDuplicidadeResponse> resultado = useCase.buscarPorSolicitacaoId(5L);

            assertNotNull(resultado);
            verify(processoAppRepository).findById(5L);
        }

        @Test
        @DisplayName("Deve lançar ObjetoNaoEncontradoException quando ID não existir")
        void deveLancarExcecaoQuandoIdNaoEncontrado() {
            when(processoAppRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(br.com.sgpj.exception.ObjetoNaoEncontradoException.class,
                    () -> useCase.buscarPorSolicitacaoId(999L));
        }
    }
}
