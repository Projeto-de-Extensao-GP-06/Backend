package br.com.sgpj.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Utilitário para hash de códigos OTP (One-Time Password).
 *
 * <p>Esta classe fornece métodos para hash e verificação de códigos OTP
 * usando BCrypt, garantindo que os códigos não sejam armazenados em texto puro
 * no banco de dados, seguindo as melhores práticas de segurança.</p>
 *
 * <p><b>Propósito de segurança:</b></p>
 * <p>Previne a exposição de códigos OTP válidos em caso de acesso não autorizado
 * ao banco de dados, evitando vulnerabilidades OWASP A02:2021 (Cryptographic Failures).</p>
 */
@Component
public class OtpHashUtil {

    private final BCryptPasswordEncoder passwordEncoder;

    public OtpHashUtil() {
        // BCrypt com força padrão de 10, adequado para OTPs de uso único
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Gera o hash de um código OTP.
     *
     * @param otp código OTP em texto puro
     * @return hash BCrypt do código OTP
     */
    public String hashOtp(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP não pode ser nulo ou vazio");
        }
        return passwordEncoder.encode(otp);
    }

    /**
     * Verifica se um código OTP em texto puro corresponde ao hash armazenado.
     *
     * @param plainOtp código OTP em texto puro
     * @param hashedOtp hash BCrypt armazenado
     * @return true se o código corresponder ao hash, false caso contrário
     */
    public boolean verifyOtp(String plainOtp, String hashedOtp) {
        if (plainOtp == null || hashedOtp == null) {
            return false;
        }
        return passwordEncoder.matches(plainOtp, hashedOtp);
    }
}
