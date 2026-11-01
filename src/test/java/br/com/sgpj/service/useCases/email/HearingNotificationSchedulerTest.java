package br.com.sgpj.service.useCases.email;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do HearingNotificationScheduler")
class HearingNotificationSchedulerTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private EmailService emailService;

    private HearingNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new HearingNotificationScheduler(processoAppRepository, emailService);
    }

    @Test
    @DisplayName("Não deve enviar e-mail semanal quando não houver audiências na semana")
    void naoDeveEnviarEmailSemanalQuandoNaoHouverAudiencias() {
        LocalDate domingo = LocalDate.of(2026, 10, 25);
        when(processoAppRepository.buscarAudienciasPorPeriodo(
                StatusProcesso.ATIVO,
                domingo.atStartOfDay(),
                domingo.plusDays(7).atStartOfDay()
        )).thenReturn(Collections.emptyList());

        scheduler.enviarNotificacoesSemanais(domingo);

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Deve consolidar audiências da semana por usuário responsável e isolar falhas de envio")
    void deveConsolidarAudienciasDaSemanaPorUsuarioEIsolarFalhas() {
        LocalDate domingo = LocalDate.of(2026, 10, 25);
        LocalDate sabado = LocalDate.of(2026, 10, 31);

        Usuario u1 = criarUsuario(1L, "Advogado 1", "adv1@sgpj.com.br");
        Usuario u2 = criarUsuario(2L, "Advogado 2", "adv2@sgpj.com.br");

        ProcessoApp p1 = criarProcesso(10L, u1, LocalDateTime.of(2026, 10, 26, 10, 0));
        ProcessoApp p2 = criarProcesso(11L, u2, LocalDateTime.of(2026, 10, 27, 14, 0));
        ProcessoApp p3 = criarProcesso(12L, u1, LocalDateTime.of(2026, 10, 29, 16, 0));

        when(processoAppRepository.buscarAudienciasPorPeriodo(
                StatusProcesso.ATIVO,
                domingo.atStartOfDay(),
                domingo.plusDays(7).atStartOfDay()
        )).thenReturn(List.of(p1, p2, p3));

        doThrow(new RuntimeException("Erro SMTP simulado"))
                .when(emailService)
                .sendWeeklyHearings(eq("adv1@sgpj.com.br"), eq("Advogado 1"), eq(domingo), eq(sabado), eq(List.of(p1, p3)));

        scheduler.enviarNotificacoesSemanais(domingo);

        verify(emailService).sendWeeklyHearings("adv1@sgpj.com.br", "Advogado 1", domingo, sabado, List.of(p1, p3));
        verify(emailService).sendWeeklyHearings("adv2@sgpj.com.br", "Advogado 2", domingo, sabado, List.of(p2));
    }

    @Test
    @DisplayName("Não deve enviar e-mail diário quando não houver audiências no dia")
    void naoDeveEnviarEmailDiarioQuandoNaoHouverAudiencias() {
        LocalDate hoje = LocalDate.of(2026, 10, 26);
        when(processoAppRepository.buscarAudienciasPorPeriodo(
                StatusProcesso.ATIVO,
                hoje.atStartOfDay(),
                hoje.plusDays(1).atStartOfDay()
        )).thenReturn(Collections.emptyList());

        scheduler.enviarNotificacoesDiarias(hoje);

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Deve consolidar audiências do dia por usuário responsável e isolar falhas de envio")
    void deveConsolidarAudienciasDoDiaPorUsuarioEIsolarFalhas() {
        LocalDate hoje = LocalDate.of(2026, 10, 26);

        Usuario u1 = criarUsuario(1L, "Advogado 1", "adv1@sgpj.com.br");
        Usuario u2 = criarUsuario(2L, "Advogado 2", "adv2@sgpj.com.br");

        ProcessoApp p1 = criarProcesso(10L, u1, LocalDateTime.of(2026, 10, 26, 9, 0));
        ProcessoApp p2 = criarProcesso(11L, u1, LocalDateTime.of(2026, 10, 26, 15, 30));
        ProcessoApp p3 = criarProcesso(12L, u2, LocalDateTime.of(2026, 10, 26, 11, 0));

        when(processoAppRepository.buscarAudienciasPorPeriodo(
                StatusProcesso.ATIVO,
                hoje.atStartOfDay(),
                hoje.plusDays(1).atStartOfDay()
        )).thenReturn(List.of(p1, p2, p3));

        doThrow(new RuntimeException("Erro SMTP simulado"))
                .when(emailService)
                .sendDailyHearings(eq("adv1@sgpj.com.br"), eq("Advogado 1"), eq(hoje), eq(List.of(p1, p2)));

        scheduler.enviarNotificacoesDiarias(hoje);

        verify(emailService).sendDailyHearings("adv1@sgpj.com.br", "Advogado 1", hoje, List.of(p1, p2));
        verify(emailService).sendDailyHearings("adv2@sgpj.com.br", "Advogado 2", hoje, List.of(p3));
    }

    private Usuario criarUsuario(Long id, String nome, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(email);
        u.setAtivo(true);
        return u;
    }

    private ProcessoApp criarProcesso(Long id, Usuario responsavel, LocalDateTime dataAudiencia) {
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial("Cliente Teste");

        ProcessoApp p = new ProcessoApp();
        p.setId(id);
        p.setUsuarioResponsavel(responsavel);
        p.setCliente(cliente);
        p.setStatus(StatusProcesso.ATIVO);
        p.setDataAudiencia(dataAudiencia);
        return p;
    }
}
