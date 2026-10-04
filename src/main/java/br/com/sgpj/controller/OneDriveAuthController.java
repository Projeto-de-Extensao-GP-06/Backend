package br.com.sgpj.controller;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.service.OneDriveReconexaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api-onedrive/auth")
@Tag(name = "OneDrive Auth")
@Validated
public class OneDriveAuthController {

    private final OneDriveTokenAdapter tokenAdapter;
    private final OneDriveReconexaoService reconexaoService;

    @Value("${microsoft.client-id}")
    private String clientId;

    @Value("${microsoft.tenant-id}")
    private String tenantId;

    @Value("${microsoft.redirect-uri}")
    private String redirectUri;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public OneDriveAuthController(OneDriveTokenAdapter tokenAdapter, OneDriveReconexaoService reconexaoService) {
        this.tokenAdapter = tokenAdapter;
        this.reconexaoService = reconexaoService;
    }

    @GetMapping("/login")
    @PreAuthorize("hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Retorna a URL para iniciar o re-login do OneDrive no navegador")
    public ResponseEntity<Map<String, String>> iniciarLogin() {
        return ResponseEntity.ok(Map.of(
                "authUrl", construirUrlAutorizacao(),
                "redirectUri", redirectUri,
                "message", "Abra a URL retornada no navegador para concluir o login da Microsoft."
        ));
    }

    @GetMapping("/login/redirect")
    @PreAuthorize("hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Redireciona o navegador para o login da Microsoft")
    public ResponseEntity<Void> redirecionarParaLogin() {
        URI location = URI.create(construirUrlAutorizacao());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build();
    }

    @GetMapping("/callback")
    @Operation(summary = "Recebe o authorization code da Microsoft e redireciona ao frontend")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription
    ) {
        boolean sucesso = reconexaoService.concluir(code, error, errorDescription);

        URI destino = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/app/perfil")
                .queryParam("onedrive", sucesso ? "connected" : "error")
                .build()
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(destino)
                .build();
    }

    private void validarCode(@NotBlank String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("O parâmetro 'code' é obrigatório no callback do OneDrive.");
        }
    }

    private String construirUrlAutorizacao() {
        return UriComponentsBuilder
                .fromUriString("https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/authorize")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_mode", "query")
                .queryParam("scope", OneDriveTokenAdapter.MICROSOFT_GRAPH_SCOPE)
                .buildAndExpand(tenantId)
                .encode()
                .toUriString();
    }

    private String formatarMensagemMicrosoft(String error, String errorDescription) {
        String codigo = URLDecoder.decode(error, StandardCharsets.UTF_8);
        if (errorDescription == null || errorDescription.isBlank()) {
            return codigo;
        }

        return codigo + " - " + URLDecoder.decode(errorDescription, StandardCharsets.UTF_8);
    }
}
