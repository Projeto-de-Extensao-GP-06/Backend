package br.com.sgpj.service;

import br.com.sgpj.adapter.database.OtpPersistenciaAdapter;
import br.com.sgpj.entity.Otp;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int EXPIRE_MINS = 15;

    private final SecureRandom secureRandom = new SecureRandom();

    private final OtpPersistenciaAdapter otpPersistenciaAdapter;

    public String generateOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(EXPIRE_MINS);

        Otp usuarioOtp = new Otp(email, otp, expiryTime);
        otpPersistenciaAdapter.save(usuarioOtp);

        return otp;
    }

    public boolean isOtpValid(String email, String inputOtp) {
        if (inputOtp != null) {
            inputOtp = inputOtp.trim();
        }

        Otp otp = otpPersistenciaAdapter.findByEmail(email);

        if (otp.isExpired()) {
            return false;
        }

        if (otp.getOtp().equals(inputOtp)) {
            otpPersistenciaAdapter.deleteByEmail(email);
            return true;
        }

        return false;
    }

    public void removeOtp(String email) {
        otpPersistenciaAdapter.deleteByEmail(email);
    }

    // Método que roda a cada 10min para limpar OTPs expiradas
    @Scheduled(cron = "0 */10 * * * *")
    public void executeCleanup() {
        otpPersistenciaAdapter.deleteExpiredBefore(LocalDateTime.now());
    }
}
