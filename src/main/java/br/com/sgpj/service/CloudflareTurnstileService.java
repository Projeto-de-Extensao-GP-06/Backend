package br.com.sgpj.service;

import br.com.sgpj.dto.response.TurnstileVerifyResponse;
import br.com.sgpj.exception.CaptchaInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CloudflareTurnstileService {

    private static final Logger log = LoggerFactory.getLogger(CloudflareTurnstileService.class);
    private static final String CLOUDFLARE_SITEVERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final RestTemplate restTemplate;
    private final String secretKey;

    public CloudflareTurnstileService(
            RestTemplate restTemplate,
            @Value("${cloudflare.secret-key}") String secretKey
    ) {
        this.restTemplate = restTemplate;
        this.secretKey = secretKey;
    }

    public void validarToken(String token, String remoteIp) {
        if (token == null || token.isBlank()) {
            throw new CaptchaInvalidoException("Token de verificação anti-robô não fornecido.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", token);
        if (remoteIp != null && !remoteIp.isBlank()) {
            body.add("remoteip", remoteIp);
        }

        try {
            TurnstileVerifyResponse response = restTemplate.postForObject(
                    CLOUDFLARE_SITEVERIFY_URL,
                    new HttpEntity<>(body, headers),
                    TurnstileVerifyResponse.class
            );

            if (response == null || !response.success()) {
                log.warn("Falha na validação do Cloudflare Turnstile. Error codes: {}",
                        response != null ? response.errorCodes() : "resposta nula");
                throw new CaptchaInvalidoException("Falha na verificação anti-robô (Cloudflare Turnstile).");
            }
        } catch (RestClientException ex) {
            log.error("Erro de comunicação com a API do Cloudflare Turnstile: {}", ex.getMessage());
            throw new CaptchaInvalidoException("Erro ao comunicar com o serviço de verificação Cloudflare.");
        }
    }
}
