package br.com.sgpj.controller;

import br.com.sgpj.service.OneDriveReconexaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@Validated
@Tag(name = "OneDrive Auth")
public class OneDriveAzureCallbackController {

    private final OneDriveReconexaoService reconexaoService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public OneDriveAzureCallbackController(OneDriveReconexaoService reconexaoService) {
        this.reconexaoService = reconexaoService;
    }

    @GetMapping("/login/oauth2/code/azure")
    @Operation(summary = "Recebe o callback da Microsoft no redirect URI legado configurado no Azure e redireciona ao frontend")
    public ResponseEntity<Void> callbackLegado(
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
}
