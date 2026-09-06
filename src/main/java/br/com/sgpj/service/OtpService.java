package br.com.sgpj.service;

import br.com.sgpj.entity.Otp;
import br.com.sgpj.exception.LimiteTentativasExcedidoException;
import br.com.sgpj.exception.OtpGenerationLimitExceededException;
import br.com.sgpj.repository.OtpRepository;
import br.com.sgpj.security.OtpHashUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Serviço para gerenciamento de códigos OTP (One-Time Password).
 *
 * <p><b>Segurança:</b> Os códigos OTP são armazenados como hash BCrypt no banco de dados,
 * não em texto puro, para evitar exposição em caso de acesso não autorizado.</p>
 */
@Service
public class OtpService {

    private static final int EXPIRE_MINS = 15;
    private static final int MAX_TENTATIVAS = 3;
    private static final int MAX_OTP_GENERATION_PER_MINUTE = 2;
    private static final int MAX_OTP_GENERATION_PER_HOUR = 5;

    private final SecureRandom secureRandom = new SecureRandom();
    private final OtpRepository otpRepository;
    private final OtpHashUtil otpHashUtil;
    private final ConcurrentMap<String, OtpGenerationHistory> otpGenerationHistory = new ConcurrentHashMap<>();

    public OtpService(OtpRepository otpRepository, OtpHashUtil otpHashUtil) {
        this.otpRepository = otpRepository;
        this.otpHashUtil = otpHashUtil;
    }

    public String generateOtp(String email) {
        checkOtpGenerationRateLimit(email);
        
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(EXPIRE_MINS);

        // Hash do OTP antes de armazenar no banco
        String hashedOtp = otpHashUtil.hashOtp(otp);
        Otp usuarioOtp = new Otp(email, hashedOtp, expiryTime);
        otpRepository.save(usuarioOtp);

        // Record this OTP generation
        recordOtpGeneration(email);

        return otp;
    }

    /**
     * Checks if the email has exceeded the OTP generation rate limit.
     * 
     * @param email Email address to check
     * @throws OtpGenerationLimitExceededException if rate limit is exceeded
     */
    private void checkOtpGenerationRateLimit(String email) {
        String key = email.toLowerCase().trim();
        OtpGenerationHistory history = otpGenerationHistory.computeIfAbsent(key, k -> new OtpGenerationHistory());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minuteWindowStart = now.minusMinutes(1);
        LocalDateTime hourWindowStart = now.minusHours(1);
        
        int minuteCount = history.cleanupAndCount(minuteWindowStart);
        int hourCount = history.cleanupAndCount(hourWindowStart);
        
        if (minuteCount >= MAX_OTP_GENERATION_PER_MINUTE) {
            throw new OtpGenerationLimitExceededException(
                "Limite de geração de OTP por minuto excedido. Tente novamente em 1 minuto.");
        }
        
        if (hourCount >= MAX_OTP_GENERATION_PER_HOUR) {
            throw new OtpGenerationLimitExceededException(
                "Limite de geração de OTP por hora excedido. Tente novamente mais tarde.");
        }
    }

    /**
     * Records an OTP generation for rate limiting purposes.
     * 
     * @param email Email address
     */
    private void recordOtpGeneration(String email) {
        String key = email.toLowerCase().trim();
        OtpGenerationHistory history = otpGenerationHistory.get(key);
        if (history != null) {
            history.addGeneration(LocalDateTime.now());
        }
    }

    /**
     * Inner class to track OTP generation history for rate limiting.
     */
    private static class OtpGenerationHistory {
        private final java.util.concurrent.CopyOnWriteArrayList<LocalDateTime> generations = 
            new java.util.concurrent.CopyOnWriteArrayList<>();
        
        public void addGeneration(LocalDateTime timestamp) {
            generations.add(timestamp);
        }
        
        public int cleanupAndCount(LocalDateTime windowStart) {
            generations.removeIf(timestamp -> timestamp.isBefore(windowStart));
            return generations.size();
        }
        
        public boolean isEmpty() {
            return generations.isEmpty();
        }
    }

    public boolean isOtpValid(String email, String inputOtp) {
        if (inputOtp != null) {
            inputOtp = inputOtp.trim();
        }

        Otp otp = otpRepository.findByEmail(email);

        if (otp.isExpired()) {
            removeOtp(email);
            return false;
        }

        // Verificar o OTP usando hash
        if (otpHashUtil.verifyOtp(inputOtp, otp.getOtp())) {
            otpRepository.deleteByEmail(email);
            return true;
        }

        otp.registrarTentativaIncorreta();

        if (otp.excedeuLimiteDeTentativas(MAX_TENTATIVAS)) {
            removeOtp(email);
            throw new LimiteTentativasExcedidoException("Número máximo de tentativas alcançado. Solicite um novo código.");
        }

        otpRepository.save(otp);
        return false;
    }

    public void removeOtp(String email) {
        otpRepository.deleteByEmail(email);
    }

    // Método que roda a cada 10min para limpar OTPs expiradas
    @Scheduled(cron = "0 */10 * * * *")
    public void executeCleanup() {
        otpRepository.deleteByExpiraEmBefore(LocalDateTime.now());
        cleanupOtpGenerationHistory();
    }

    /**
     * Cleans up old OTP generation history to prevent memory leaks.
     * Removes entries older than 1 hour.
     */
    private void cleanupOtpGenerationHistory() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        otpGenerationHistory.entrySet().removeIf(entry -> {
            OtpGenerationHistory history = entry.getValue();
            history.cleanupAndCount(cutoff);
            return history.isEmpty();
        });
    }
}
