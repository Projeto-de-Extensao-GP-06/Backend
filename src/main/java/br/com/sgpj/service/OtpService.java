package br.com.sgpj.service;

import br.com.sgpj.adapter.database.OtpPersistenciaAdapter;
import br.com.sgpj.entity.Otp;
import br.com.sgpj.exception.LimiteTentativasExcedidoException;
import br.com.sgpj.security.OtpHashUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

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

    private final SecureRandom secureRandom = new SecureRandom();
    private final OtpPersistenciaAdapter otpPersistenciaAdapter;
    private final OtpHashUtil otpHashUtil;

    public OtpService(OtpPersistenciaAdapter otpPersistenciaAdapter, OtpHashUtil otpHashUtil) {
        this.otpPersistenciaAdapter = otpPersistenciaAdapter;
        this.otpHashUtil = otpHashUtil;
    }

    public String generateOtp(String email) {
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(EXPIRE_MINS);

        // Hash do OTP antes de armazenar no banco
        String hashedOtp = otpHashUtil.hashOtp(otp);
        Otp usuarioOtp = new Otp(email, hashedOtp, expiryTime);
        otpPersistenciaAdapter.save(usuarioOtp);

        return otp;
    }

    public boolean isOtpValid(String email, String inputOtp) {
        if (inputOtp != null) {
            inputOtp = inputOtp.trim();
        }

        Otp otp = otpPersistenciaAdapter.findByEmail(email);

        if (otp.isExpired()) {
            removeOtp(email);
            return false;
        }

        // Verificar o OTP usando hash
        if (otpHashUtil.verifyOtp(inputOtp, otp.getOtp())) {
            otpPersistenciaAdapter.deleteByEmail(email);
            return true;
        }

        otp.registrarTentativaIncorreta();

        if (otp.excedeuLimiteDeTentativas(MAX_TENTATIVAS)) {
            removeOtp(email);
            throw new LimiteTentativasExcedidoException("Número máximo de tentativas alcançado. Solicite um novo código.");
        }

        otpPersistenciaAdapter.save(otp);
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
