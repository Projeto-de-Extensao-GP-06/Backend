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

    private record OtpData(String otp, LocalDateTime expiryTime) {}
}
