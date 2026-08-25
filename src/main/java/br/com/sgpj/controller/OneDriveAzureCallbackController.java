package br.com.sgpj.controller;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.exception.OneDriveErroException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@Validated
@Tag(name = "OneDrive Auth")
public class OneDriveAzureCallbackController {

    private final OneDriveTokenAdapter tokenAdapter;

    public OneDriveAzureCallbackController(OneDriveTokenAdapter tokenAdapter) {
        this.tokenAdapter = tokenAdapter;
    }

    @GetMapping("/login/oauth2/code/azure")
    @Operation(summary = "Recebe o callback da Microsoft no redirect URI legado configurado no Azure")
    public ResponseEntity<Map<String, String>> callbackLegado(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription
    ) {
        if (error != null && !error.isBlank()) {
            throw new OneDriveErroException("A Microsoft recusou a autenticação do OneDrive: "
                    + formatarMensagemMicrosoft(error, errorDescription));
        }

        validarCode(code);
        tokenAdapter.trocarCodigoPorToken(code);

        return ResponseEntity.ok(Map.of(
                "message", "OneDrive reconectado com sucesso.",
                "status", "connected"
        ));
    }

    private void validarCode(@NotBlank String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("O parâmetro 'code' é obrigatório no callback do OneDrive.");
        }
    }

    private String formatarMensagemMicrosoft(String error, String errorDescription) {
        String codigo = URLDecoder.decode(error, StandardCharsets.UTF_8);
        if (errorDescription == null || errorDescription.isBlank()) {
            return codigo;
        }

        return codigo + " - " + URLDecoder.decode(errorDescription, StandardCharsets.UTF_8);
    }
}
