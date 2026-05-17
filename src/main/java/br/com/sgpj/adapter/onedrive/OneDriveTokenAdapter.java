package br.com.sgpj.adapter.onedrive;

import br.com.sgpj.entity.onedrive.MicrosoftTokenConfig;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.repository.MicrosoftTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class OneDriveTokenAdapter {

    @Value("${microsoft.client-id}")
    private String clientId;

    @Value("${microsoft.client-secret}")
    private String clientSecret;

    @Value("${microsoft.tenant-id}")
    private String tenantId;

    private String accessToken;
    private LocalDateTime expiresAt;

    private final MicrosoftTokenRepository tokenRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public OneDriveTokenAdapter(MicrosoftTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }


    //  Retorna um access token válido. Renova automaticamente se expirado.
    public synchronized String getAccessToken() {
        if (accessToken != null && expiresAt != null
                && LocalDateTime.now().isBefore(expiresAt.minusMinutes(5))) {
            return accessToken;
        }
        return renovarToken();
    }

    private String renovarToken() {
        MicrosoftTokenConfig registro = tokenRepository.findById(1L)
                .orElseThrow(() -> new OneDriveErroException(
                        "Refresh token não encontrado no banco. Execute o SetupOneDrive primeiro."));

        String refreshToken = registro.getRefreshToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token",
                new HttpEntity<>(body, headers),
                Map.class
        );

        if (response == null) {
            throw new OneDriveErroException("Resposta nula ao renovar token Microsoft.");
        }

        this.accessToken = (String) response.get("access_token");
        this.expiresAt = LocalDateTime.now().plusSeconds(
                Long.parseLong(response.get("expires_in").toString())
        );

        // A Microsoft rotaciona o refresh token a cada uso — persiste o novo
        registro.setRefreshToken((String) response.get("refresh_token"));
        registro.setDataCriacao(LocalDateTime.now());
        tokenRepository.save(registro);

        return this.accessToken;
    }
}

