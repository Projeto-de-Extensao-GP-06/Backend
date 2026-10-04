package br.com.sgpj.service;

import br.com.sgpj.dto.response.Dashboard.DadosFiltradosDashResponse;
import br.com.sgpj.dto.response.Dashboard.DadosIndependentesResponse;
import br.com.sgpj.dto.response.Dashboard.FunilAtendimentoResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Tests")
class DashboardServiceTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private FaseProcessoRepository faseProcessoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private StatusCliente statusLead;
    private StatusCliente statusAtivo;

    @BeforeEach
    void setUp() {
        statusLead = new StatusCliente();
        statusLead.setId(1L);
        statusLead.setNome("LEAD");

        statusAtivo = new StatusCliente();
        statusAtivo.setId(2L);
        statusAtivo.setNome("ATIVO");
    }

    @Nested
    @DisplayName("extrairFunilAtendimento")
    class ExtrairFunilAtendimentoTests {

        @Test
        @DisplayName("Deve extrair funil de atendimento calculando solicitacoes recebidas e aceitas no periodo")
        void deveExtrairFunilAtendimentoComSucesso() {
            LocalDate dateFrom = LocalDate.of(2026, 1, 1);
            LocalDate dateTo = LocalDate.of(2026, 1, 31);

            // Solicitação 1: recebida no período, pendente (LEAD)
            Cliente cliente1 = new Cliente();
            cliente1.setStatusCliente(statusLead);
            cliente1.setDataHoraEnvioFormulario(LocalDateTime.of(2026, 1, 10, 10, 0));

            ProcessoApp solicitacao1 = new ProcessoApp();
            solicitacao1.setCliente(cliente1);
            solicitacao1.setStatus(StatusProcesso.SOLICITACAO);
            solicitacao1.setDataHoraInicio(LocalDateTime.of(2026, 1, 10, 10, 0));

            // Solicitação 2: recebida no período, aceita (ATIVO)
            Cliente cliente2 = new Cliente();
            cliente2.setStatusCliente(statusAtivo);
            cliente2.setDataHoraEnvioFormulario(LocalDateTime.of(2026, 1, 15, 14, 30));
            cliente2.setDataHoraTransformacaoEmCliente(LocalDateTime.of(2026, 1, 16, 9, 0));

            ProcessoApp solicitacao2 = new ProcessoApp();
            solicitacao2.setCliente(cliente2);
            solicitacao2.setStatus(StatusProcesso.ATIVO);
            solicitacao2.setDataHoraInicio(LocalDateTime.of(2026, 1, 15, 14, 30));

            // Solicitação 3: recebida fora do período (em fevereiro)
            Cliente cliente3 = new Cliente();
            cliente3.setStatusCliente(statusLead);
            cliente3.setDataHoraEnvioFormulario(LocalDateTime.of(2026, 2, 5, 8, 0));

            ProcessoApp solicitacao3 = new ProcessoApp();
            solicitacao3.setCliente(cliente3);
            solicitacao3.setStatus(StatusProcesso.SOLICITACAO);
            solicitacao3.setDataHoraInicio(LocalDateTime.of(2026, 2, 5, 8, 0));

            List<ProcessoApp> solicitacoes = List.of(solicitacao1, solicitacao2, solicitacao3);

            FunilAtendimentoResponse funil = dashboardService.extrairFunilAtendimento(dateFrom, dateTo, solicitacoes);

            assertNotNull(funil);
            assertEquals(2, funil.recebidas());
            assertEquals(1, funil.aceitas());
        }

        @Test
        @DisplayName("Deve retornar zeros quando nenhuma solicitacao estiver no periodo")
        void deveRetornarZerosQuandoNenhumaNoPeriodo() {
            LocalDate dateFrom = LocalDate.of(2026, 3, 1);
            LocalDate dateTo = LocalDate.of(2026, 3, 31);

            Cliente cliente = new Cliente();
            cliente.setStatusCliente(statusLead);
            cliente.setDataHoraEnvioFormulario(LocalDateTime.of(2026, 1, 10, 10, 0));

            ProcessoApp solicitacao = new ProcessoApp();
            solicitacao.setCliente(cliente);
            solicitacao.setStatus(StatusProcesso.SOLICITACAO);
            solicitacao.setDataHoraInicio(LocalDateTime.of(2026, 1, 10, 10, 0));

            FunilAtendimentoResponse funil = dashboardService.extrairFunilAtendimento(dateFrom, dateTo, List.of(solicitacao));

            assertNotNull(funil);
            assertEquals(0, funil.recebidas());
            assertEquals(0, funil.aceitas());
        }
    }

    @Nested
    @DisplayName("listarDadosFiltradosDash")
    class ListarDadosFiltradosDashTests {

        @Test
        @DisplayName("Deve retornar dados filtrados incluindo o funil de atendimento")
        void deveListarDadosFiltradosDash() {
            LocalDate dateFrom = LocalDate.of(2026, 1, 1);
            LocalDate dateTo = LocalDate.of(2026, 1, 31);

            when(processoAppRepository.findAll()).thenReturn(new ArrayList<>());
            when(clienteRepository.findAll()).thenReturn(new ArrayList<>());
            when(faseProcessoRepository.findAll()).thenReturn(new ArrayList<>());

            DadosFiltradosDashResponse response = dashboardService.listarDadosFiltradosDash(dateFrom, dateTo);

            assertNotNull(response);
            assertNotNull(response.funilAtendimento());
            assertNotNull(response.processosEncerrados());
            assertNotNull(response.processosSuspensos());
            assertNotNull(response.evolucaoCarteira());
            assertNotNull(response.formasDeEntrada());
        }
    }
}
