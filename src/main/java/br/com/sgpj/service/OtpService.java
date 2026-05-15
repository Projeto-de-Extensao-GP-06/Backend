package br.com.sgpj.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int EXPIRE_MINS = 15;

    private final ConcurrentHashMap<String, OtpData> otpCache = new ConcurrentHashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();

    private record OtpData(String otp, LocalDateTime expiryTime) {
        boolean isExpired(LocalDateTime expiryTime) {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    public String generateOtp(String email) {

        // Validação para evitar chaves duplicadas no otpCache
        if (otpCache.containsKey(email)) {
            otpCache.remove(email);
        }

        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(EXPIRE_MINS);
        otpCache.put(email, new OtpData(otp, expiryTime));

        return otp;
    }

    public boolean isOtpValid(String email, String inputOtp) {
        if (inputOtp != null) {
            inputOtp = inputOtp.trim();
        }
        OtpData data = otpCache.get(email);

        if (data == null) {
            return false;
        }

        if (data.isExpired(data.expiryTime)) {
            otpCache.remove(email);
            return false;
        }

        if (data.otp().equals(inputOtp)) {
            return true;
        }

        return false;
    }

    public void removeOtp(String email) {
        otpCache.remove(email);
    }
}
