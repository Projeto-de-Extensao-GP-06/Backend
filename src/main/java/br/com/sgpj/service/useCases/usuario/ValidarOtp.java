package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.service.OtpService;
import org.springframework.stereotype.Service;

@Service
public class ValidarOtp {

    private final OtpService otpService;

    public ValidarOtp(OtpService otpService) {
        this.otpService = otpService;
    }

    public boolean executar(ValidarOtpRequest request) {

        validarRequest(request);

        return otpService.isOtpValid(
                request.email(),
                request.otp()
        );
    }

    private void validarRequest(ValidarOtpRequest request) {

        if (request.email() == null
                || request.email().isBlank()) {

            throw new IllegalArgumentException(
                    "Insira um endereço de e-mail válido"
            );
        }

        if (request.otp() == null
                || request.otp().isBlank()) {

            throw new IllegalArgumentException(
                    "Insira uma OTP válida."
            );
        }
    }
}