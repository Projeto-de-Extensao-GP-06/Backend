package br.com.sgpj.service.useCases.email;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.service.EmailService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class HearingNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(HearingNotificationScheduler.class);
    private static final String TIMEZONE_BRASIL = "America/Sao_Paulo";
    private static final ZoneId ZONE_BRASIL = ZoneId.of(TIMEZONE_BRASIL);

    private final ProcessoAppRepository processoAppRepository;
    private final EmailService emailService;

    public HearingNotificationScheduler(ProcessoAppRepository processoAppRepository, EmailService emailService) {
        this.processoAppRepository = processoAppRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 1 * * SUN", zone = TIMEZONE_BRASIL)
    @SchedulerLock(
            name = "HearingNotificationScheduler_enviarNotificacoesSemanais",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT15M"
    )
    public void enviarNotificacoesSemanais() {
        enviarNotificacoesSemanais(LocalDate.now(ZONE_BRASIL));
    }

    public void enviarNotificacoesSemanais(LocalDate inicioSemana) {
        LocalDate fimSemana = inicioSemana.plusDays(6);
        LocalDateTime dataInicio = inicioSemana.atStartOfDay();
        LocalDateTime dataFimExclusiva = inicioSemana.plusDays(7).atStartOfDay();

        List<ProcessoApp> processos = processoAppRepository.buscarAudienciasPorPeriodo(
                StatusProcesso.ATIVO, dataInicio, dataFimExclusiva);

        if (processos.isEmpty()) {
            return;
        }

        Map<Long, List<ProcessoApp>> processosPorUsuario = agruparPorUsuarioResponsavel(processos);

        for (List<ProcessoApp> processosDoUsuario : processosPorUsuario.values()) {
            Usuario usuario = processosDoUsuario.getFirst().getUsuarioResponsavel();
            try {
                emailService.sendWeeklyHearings(
                        usuario.getEmail(),
                        usuario.getNome(),
                        inicioSemana,
                        fimSemana,
                        processosDoUsuario
                );
            } catch (Exception e) {
                log.error("Falha ao enviar e-mail de audiências da semana para o usuário ID {}: {}",
                        usuario.getId(), e.getMessage(), e);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *", zone = TIMEZONE_BRASIL)
    @SchedulerLock(
            name = "HearingNotificationScheduler_enviarNotificacoesDiarias",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT15M"
    )
    public void enviarNotificacoesDiarias() {
        enviarNotificacoesDiarias(LocalDate.now(ZONE_BRASIL));
    }

    public void enviarNotificacoesDiarias(LocalDate dataReferencia) {
        LocalDateTime dataInicio = dataReferencia.atStartOfDay();
        LocalDateTime dataFimExclusiva = dataReferencia.plusDays(1).atStartOfDay();

        List<ProcessoApp> processos = processoAppRepository.buscarAudienciasPorPeriodo(
                StatusProcesso.ATIVO, dataInicio, dataFimExclusiva);

        if (processos.isEmpty()) {
            return;
        }

        Map<Long, List<ProcessoApp>> processosPorUsuario = agruparPorUsuarioResponsavel(processos);

        for (List<ProcessoApp> processosDoUsuario : processosPorUsuario.values()) {
            Usuario usuario = processosDoUsuario.getFirst().getUsuarioResponsavel();
            try {
                emailService.sendDailyHearings(
                        usuario.getEmail(),
                        usuario.getNome(),
                        dataReferencia,
                        processosDoUsuario
                );
            } catch (Exception e) {
                log.error("Falha ao enviar e-mail de audiências do dia para o usuário ID {}: {}",
                        usuario.getId(), e.getMessage(), e);
            }
        }
    }

    private Map<Long, List<ProcessoApp>> agruparPorUsuarioResponsavel(List<ProcessoApp> processos) {
        Map<Long, List<ProcessoApp>> agrupados = new LinkedHashMap<>();
        for (ProcessoApp processo : processos) {
            Usuario responsavel = processo.getUsuarioResponsavel();
            if (responsavel != null && responsavel.getId() != null) {
                agrupados.computeIfAbsent(responsavel.getId(), k -> new ArrayList<>()).add(processo);
            }
        }
        return agrupados;
    }
}
