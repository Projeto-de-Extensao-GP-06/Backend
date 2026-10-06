package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.exception.OtpInvalidaException;
import br.com.sgpj.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidarOtp {

    private final OtpService otpService;

    public ValidarOtp(OtpService otpService) {
        this.otpService = otpService;
    }

    public void executar(ValidarOtpRequest request) {

        validarRequest(request);

        if (!otpService.isOtpValid(request.email(), request.otp())) {
            log.warn("Falha ao validar OTP. Motivo: código inválido para o e-mail {}", request.email());
            throw new OtpInvalidaException("Código de verificação inválido.");
        }

        log.info("Código de verificação válido para o usuário {}.", request.email());
    }

    private void validarRequest(ValidarOtpRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Insira um payload válido.");
        }

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