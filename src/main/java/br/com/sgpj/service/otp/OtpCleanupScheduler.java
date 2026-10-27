package br.com.sgpj.service.otp;

import br.com.sgpj.repository.OtpRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Agendador infra para limpar OTPs expiradas e histórico de rate limiter.
 * Responsabilidade de infraestrutura separada do serviço de negócio.
 */
@Component
public class OtpCleanupScheduler {

    private final OtpRepository otpRepository;
    private final OtpRateLimiter rateLimiter;

    public OtpCleanupScheduler(OtpRepository otpRepository, OtpRateLimiter rateLimiter) {
        this.otpRepository = otpRepository;
        this.rateLimiter = rateLimiter;
    }

    // roda a cada 10 minutos
    @Scheduled(cron = "0 */10 * * * *")
    @SchedulerLock(
            name = "OtpCleanupScheduler_cleanup",
            lockAtLeastFor = "PT30S",
            lockAtMostFor = "PT5M"
    )
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        otpRepository.deleteByExpiraEmBefore(now);
        // limpa entradas de rate limiter mais velhas que 1 hora
        rateLimiter.cleanupOlderThan(now.minusHours(1));
    }
}
