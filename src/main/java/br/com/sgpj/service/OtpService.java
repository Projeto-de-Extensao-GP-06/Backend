package br.com.sgpj.service;

import br.com.sgpj.entity.Otp;
import br.com.sgpj.repository.OtpRepository;
import br.com.sgpj.security.OtpHashUtil;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import br.com.sgpj.service.otp.OtpRateLimiter;
import br.com.sgpj.service.otp.AttemptPolicy;

/**
 * Serviço para gerenciamento de códigos OTP (One-Time Password).
 *
 * <p><b>Segurança:</b> Os códigos OTP são armazenados como hash BCrypt no banco de dados,
 * não em texto puro, para evitar exposição em caso de acesso não autorizado.</p>
 */
@Service
public class OtpService {

    private static final int EXPIRE_MINS = 15;

    private final SecureRandom secureRandom = new SecureRandom();
    private final OtpRepository otpRepository;
    private final OtpHashUtil otpHashUtil;
    private final OtpRateLimiter otpRateLimiter;
    private final AttemptPolicy attemptPolicy;

    public OtpService(OtpRepository otpRepository, OtpHashUtil otpHashUtil, OtpRateLimiter otpRateLimiter, AttemptPolicy attemptPolicy) {
        this.otpRepository = otpRepository;
        this.otpHashUtil = otpHashUtil;
        this.otpRateLimiter = otpRateLimiter;
        this.attemptPolicy = attemptPolicy;
    }

    public String generateOtp(String email) {
        return generateOtp(email, true);
    }

    public String generateOtp(String email, boolean usuarioValido) {
        otpRateLimiter.checkGenerationAllowed(email);

        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(EXPIRE_MINS);

        // Hash do OTP antes de armazenar no banco
        String hashedOtp = otpHashUtil.hashOtp(otp);
        Otp usuarioOtp = new Otp(email, hashedOtp, expiryTime, usuarioValido);
        otpRepository.save(usuarioOtp);

        // Record this OTP generation in the rate limiter
        otpRateLimiter.recordGeneration(email);

        return otp;
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

        if (otp == null) {
            return false;
        }

        if (otp.isExpired()) {
            removeOtp(email);
            return false;
        }

        if (otp.isUsuarioValido()) {
            // Verificar o OTP usando hash
            if (otpHashUtil.verifyOtp(inputOtp, otp.getOtp())) {
                otpRepository.deleteByEmail(email);
                return true;
            }
        }

        // Delega controle de tentativas para a política
        attemptPolicy.registerFailedAttempt(otp);

        // Se não lançar, persiste a entidade atualizada
        otpRepository.save(otp);
        return false;
    }

    public void removeOtp(String email) {
        otpRepository.deleteByEmail(email);
    }

}

