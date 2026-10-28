package br.com.sgpj.service;

import br.com.sgpj.dto.response.Dashboard.DadosFiltradosDashResponse;
import br.com.sgpj.dto.response.Dashboard.EvolucaoCarteiraResponse;
import br.com.sgpj.dto.response.Dashboard.ProcessosEncerradosResponse;
import br.com.sgpj.dto.response.Dashboard.ProcessosSuspensosResponse;
import br.com.sgpj.entity.Processo.MotivoStatusProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
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

    @Nested
    @DisplayName("Processos Encerrados e Suspensos")
    class ProcessosEncerradosESuspensosTests {

        @Test
        @DisplayName("Deve separar corretamente processos encerrados e suspensos filtrando pela data de criacao (dataHoraInicio)")
        void deveSepararProcessosEncerradosESuspensosPorDataCriacao() {
            LocalDate inicioSemana = LocalDate.of(2026, 10, 19);
            LocalDate hoje = LocalDate.of(2026, 10, 22);

            MotivoStatusProcesso motivoAcordo = new MotivoStatusProcesso();
            motivoAcordo.setNome("ACORDO");
            motivoAcordo.setAplicavelA(MotivoStatusProcesso.AplicavelA.ENCERRADO);

            ProcessoApp processoEncerrado = new ProcessoApp();
            processoEncerrado.setStatus(StatusProcesso.ENCERRADO);
            processoEncerrado.setDataHoraInicio(LocalDateTime.of(2026, 10, 20, 10, 30));
            processoEncerrado.setEncerradoSuspensoEm(LocalDateTime.of(2026, 10, 22, 15, 0));
            processoEncerrado.setMotivoStatus(motivoAcordo);

            MotivoStatusProcesso motivoTratativas = new MotivoStatusProcesso();
            motivoTratativas.setNome("TRATATIVAS");
            motivoTratativas.setAplicavelA(MotivoStatusProcesso.AplicavelA.SUSPENSO);

            ProcessoApp processoSuspenso = new ProcessoApp();
            processoSuspenso.setStatus(StatusProcesso.SUSPENSO);
            processoSuspenso.setDataHoraInicio(LocalDateTime.of(2026, 10, 22, 14, 20));
            processoSuspenso.setEncerradoSuspensoEm(LocalDateTime.of(2026, 10, 22, 16, 0));
            processoSuspenso.setMotivoStatus(motivoTratativas);

            // Processo encerrado em outro mês (não deve entrar no filtro da semana)
            ProcessoApp processoMesAnterior = new ProcessoApp();
            processoMesAnterior.setStatus(StatusProcesso.ENCERRADO);
            processoMesAnterior.setDataHoraInicio(LocalDateTime.of(2026, 2, 10, 10, 30));
            processoMesAnterior.setEncerradoSuspensoEm(LocalDateTime.of(2026, 2, 15, 15, 0));
            processoMesAnterior.setMotivoStatus(motivoAcordo);

            List<ProcessoApp> lista = List.of(processoEncerrado, processoSuspenso, processoMesAnterior);

            ProcessosEncerradosResponse encerrados = dashboardService.extrairProcessosEncerrados(inicioSemana, hoje, lista);
            ProcessosSuspensosResponse suspensos = dashboardService.extrairProcessosSuspensos(inicioSemana, hoje, lista);
            EvolucaoCarteiraResponse evolucao = dashboardService.extrairEvolucaoCarteira(inicioSemana, hoje, lista);

            assertEquals(1, encerrados.encerradosPorAcordo());
            assertEquals(0, encerrados.encerradosPorOutros());

            assertEquals(1, suspensos.suspensosPorTratativas());
            assertEquals(0, suspensos.suspensosPorOutros());

            assertEquals(1, evolucao.encerrados());
            assertEquals(1, evolucao.suspensos());
            assertEquals(2, evolucao.novos());
        }
    }

    @Nested
    @DisplayName("listarDadosFiltradosDash")
    class ListarDadosFiltradosDashTests {

        @Test
        @DisplayName("Deve retornar dados filtrados da dashboard")
        void deveListarDadosFiltradosDash() {
            LocalDate dateFrom = LocalDate.of(2026, 1, 1);
            LocalDate dateTo = LocalDate.of(2026, 1, 31);

            when(processoAppRepository.findAll()).thenReturn(new ArrayList<>());
            when(clienteRepository.findAll()).thenReturn(new ArrayList<>());
            when(faseProcessoRepository.findAll()).thenReturn(new ArrayList<>());

            DadosFiltradosDashResponse response = dashboardService.listarDadosFiltradosDash(dateFrom, dateTo);

            assertNotNull(response);
            assertNotNull(response.processosEncerrados());
            assertNotNull(response.processosSuspensos());
            assertNotNull(response.evolucaoCarteira());
            assertNotNull(response.formasDeEntrada());
        }
    }
}
