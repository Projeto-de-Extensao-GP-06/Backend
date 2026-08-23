package br.com.sgpj.adapter.onedrive;

import br.com.sgpj.entity.onedrive.MicrosoftTokenConfig;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.repository.MicrosoftTokenRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class OneDriveTokenAdapter {

    public static final String MICROSOFT_GRAPH_SCOPE = "offline_access Files.ReadWrite.All";
    private static final long TOKEN_CONFIG_ID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${microsoft.client-id}")
    private String clientId;

    @Value("${microsoft.client-secret}")
    private String clientSecret;

    @Value("${microsoft.tenant-id}")
    private String tenantId;

    @Value("${microsoft.redirect-uri}")
    private String redirectUri;

    private String accessToken;
    private LocalDateTime expiresAt;

    private final MicrosoftTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public OneDriveTokenAdapter(MicrosoftTokenRepository tokenRepository) {
        this(tokenRepository, new RestTemplate());
    }

    OneDriveTokenAdapter(MicrosoftTokenRepository tokenRepository, RestTemplate restTemplate) {
        this.tokenRepository = tokenRepository;
        this.restTemplate = restTemplate;
    }


    //  Retorna um access token válido. Renova automaticamente se expirado.
    public synchronized String getAccessToken() {
        if (accessToken != null && expiresAt != null
                && LocalDateTime.now().isBefore(expiresAt.minusMinutes(5))) {
            return accessToken;
        }
        return renovarToken();
    }

    public synchronized void trocarCodigoPorToken(String code) {
        MicrosoftTokenConfig registro = obterOuCriarRegistroToken();

        MultiValueMap<String, String> body = criarCorpoRequisicaoPadrao();
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", redirectUri);
        body.add("scope", MICROSOFT_GRAPH_SCOPE);

        Map<String, Object> response = executarRequisicaoToken(
                body,
                "Não foi possível concluir a reconexão do OneDrive. O código de autorização expirou, já foi utilizado ou foi revogado. Inicie o login novamente.",
                "Erro ao trocar o código de autorização do OneDrive."
        );

        atualizarCacheEPersistirTokens(registro, response);
    }

    private String renovarToken() {
        MicrosoftTokenConfig registro = tokenRepository.findById(TOKEN_CONFIG_ID)
                .orElseThrow(() -> new OneDriveErroException(
                        "Refresh token não encontrado no banco. Execute o login do OneDrive em /api-onedrive/auth/login."));

        String refreshToken = registro.getRefreshToken();

        MultiValueMap<String, String> body = criarCorpoRequisicaoPadrao();
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        Map<String, Object> response = executarRequisicaoToken(
                body,
                "O refresh token do OneDrive expirou, foi revogado ou está inválido. Reconecte o OneDrive em /api-onedrive/auth/login.",
                "Erro ao renovar o token do OneDrive."
        );

        atualizarCacheEPersistirTokens(registro, response);
        return this.accessToken;
    }

    private MultiValueMap<String, String> criarCorpoRequisicaoPadrao() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        return body;
    }

    private Map<String, Object> executarRequisicaoToken(
            MultiValueMap<String, String> body,
            String invalidGrantMessage,
            String genericErrorMessage
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            if (response == null) {
                throw new OneDriveErroException(genericErrorMessage + " Resposta nula da Microsoft.");
            }

            return response;
        } catch (HttpClientErrorException ex) {
            if (isInvalidGrant(ex)) {
                throw new OneDriveErroException(invalidGrantMessage);
            }

            throw new OneDriveErroException(genericErrorMessage + " " + extrairMensagemMicrosoft(ex));
        } catch (RestClientException ex) {
            throw new OneDriveErroException(genericErrorMessage + " " + ex.getMessage());
        }
    }

    private void atualizarCacheEPersistirTokens(MicrosoftTokenConfig registro, Map<String, Object> response) {
        this.accessToken = obterCampoObrigatorio(response, "access_token");
        this.expiresAt = LocalDateTime.now().plusSeconds(
                Long.parseLong(obterCampoObrigatorio(response, "expires_in"))
        );

        String novoRefreshToken = (String) response.get("refresh_token");
        if (novoRefreshToken == null || novoRefreshToken.isBlank()) {
            throw new OneDriveErroException("A Microsoft não retornou um refresh token válido para o OneDrive.");
        }

        registro.setRefreshToken(novoRefreshToken);

        registro.setDataCriacao(LocalDateTime.now());
        tokenRepository.save(registro);
    }

    private MicrosoftTokenConfig obterOuCriarRegistroToken() {
        return tokenRepository.findById(TOKEN_CONFIG_ID)
                .orElseGet(() -> {
                    MicrosoftTokenConfig config = new MicrosoftTokenConfig();
                    config.setId(TOKEN_CONFIG_ID);
                    return config;
                });
    }

    private String obterCampoObrigatorio(Map<String, Object> response, String campo) {
        Object valor = response.get(campo);
        if (valor == null || valor.toString().isBlank()) {
            throw new OneDriveErroException("A Microsoft não retornou o campo obrigatório '" + campo + "' no fluxo do OneDrive.");
        }
        return valor.toString();
    }

    private boolean isInvalidGrant(HttpClientErrorException ex) {
        String responseBody = ex.getResponseBodyAsString();
        return responseBody.contains("invalid_grant");
    }

    private String extrairMensagemMicrosoft(HttpClientErrorException ex) {
        String responseBody = ex.getResponseBodyAsString();
        if (responseBody.isBlank()) {
            return "HTTP " + ex.getStatusCode().value() + " ao chamar a Microsoft.";
        }

        try {
            Map<String, Object> body = OBJECT_MAPPER.readValue(responseBody, new TypeReference<>() {});
            Object description = body.get("error_description");
            if (description != null && !description.toString().isBlank()) {
                return description.toString();
            }

            Object error = body.get("error");
            if (error != null && !error.toString().isBlank()) {
                return error.toString();
            }
        } catch (IOException ignored) {
            // Se o body não vier em JSON, usa o texto bruto recebido.
        }

        return responseBody;
    }
}
