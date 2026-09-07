package br.com.sgpj.service.otp;

import br.com.sgpj.exception.OtpGenerationLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementação em memória do OtpRateLimiter. Encapsula o uso de estruturas concorrentes
 * de forma que a regra de negócio não dependa diretamente delas (permite trocar por Redis, etc.).
 */
@Component
public class InMemoryOtpRateLimiter implements OtpRateLimiter {

    private static final int MAX_OTP_GENERATION_PER_MINUTE = 2;
    private static final int MAX_OTP_GENERATION_PER_HOUR = 5;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<LocalDateTime>> map = new ConcurrentHashMap<>();

    @Override
    public void checkGenerationAllowed(String email) {
        String key = normalize(email);
        var list = map.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minuteWindowStart = now.minusMinutes(1);
        LocalDateTime hourWindowStart = now.minusHours(1);

        cleanupList(list, minuteWindowStart);
        int minuteCount = countSince(list, minuteWindowStart);
        if (minuteCount >= MAX_OTP_GENERATION_PER_MINUTE) {
            throw new OtpGenerationLimitExceededException("Limite de geração de OTP por minuto excedido. Tente novamente em 1 minuto.");
        }

        cleanupList(list, hourWindowStart);
        int hourCount = countSince(list, hourWindowStart);
        if (hourCount >= MAX_OTP_GENERATION_PER_HOUR) {
            throw new OtpGenerationLimitExceededException("Limite de geração de OTP por hora excedido. Tente novamente mais tarde.");
        }
    }

    @Override
    public void recordGeneration(String email) {
        String key = normalize(email);
        var list = map.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        list.add(LocalDateTime.now());
    }

    @Override
    public void cleanupOlderThan(LocalDateTime cutoff) {
        map.forEach((k, list) -> cleanupList(list, cutoff));
        map.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    private void cleanupList(List<LocalDateTime> list, LocalDateTime cutoff) {
        list.removeIf(ts -> ts.isBefore(cutoff));
    }

    private int countSince(List<LocalDateTime> list, LocalDateTime since) {
        int count = 0;
        for (LocalDateTime ts : list) {
            if (!ts.isBefore(since)) count++;
        }
        return count;
    }

    private String normalize(String email) {
        return email == null ? "" : email.toLowerCase().trim();
    }
}
