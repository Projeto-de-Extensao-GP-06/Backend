package br.com.sgpj.service;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class OneDriveReconexaoService {
    private static final Logger log = Logger.getLogger(OneDriveReconexaoService.class.getName());
    private final OneDriveTokenAdapter tokenAdapter;

    public OneDriveReconexaoService(OneDriveTokenAdapter tokenAdapter) {
        this.tokenAdapter = tokenAdapter;
    }

    public boolean concluir(String code, String error, String errorDescription) {
        if (error != null && !error.isBlank()) {
            log.warning("Microsoft recusou a autenticação: " + error + " - " + errorDescription);
            return false;
        }
        if (code == null || code.isBlank()) return false;
        try {
            tokenAdapter.exchangeCodeForToken(code);
            return true;
        } catch (Exception e) {
            log.severe("Falha ao trocar o code do OneDrive: " + e.getMessage());
            return false;
        }
    }

}
