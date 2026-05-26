package br.com.sgpj.controller;

import br.com.sgpj.dto.response.CaminhoArmazenamentoResponse;
import br.com.sgpj.dto.response.CaminhoClienteResponse;
import br.com.sgpj.dto.response.DocumentoResponse;
import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documentos")
@Tag(name = "Documentos")
@Validated
public class DocumentoController {

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) {
        this.service = service;
    }

    @GetMapping("/tipos-documento")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista todos os tipos de documento disponíveis")
    public ResponseEntity<List<TipoDocumentoResponse>> listarTiposDocumento(HttpServletRequest httpRequest) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para listar tipos de documento.");
        }

        List<TipoDocumentoResponse> tipos = service.listarTiposDocumento();
        if (tipos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/caminhos-armazenamento")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista todos os caminhos padrão de armazenamento")
    public ResponseEntity<List<CaminhoArmazenamentoResponse>> listarCaminhosArmazenamento(HttpServletRequest httpRequest) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para listar caminhos de armazenamento.");
        }

        List<CaminhoArmazenamentoResponse> caminhos = service.listarCaminhosArmazenamento();
        if (caminhos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(caminhos);
    }

    @GetMapping("/cliente/{clienteId}/caminhos")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista os caminhos de armazenamento do cliente")
    public ResponseEntity<List<CaminhoClienteResponse>> listarCaminhosDoCliente(
            @PathVariable @NotNull(message = "O ID do cliente é obrigatório") @Positive(message = "O ID do cliente deve ser um número positivo") Long clienteId,
            HttpServletRequest httpRequest
    ) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para consultar caminhos.");
        }

        List<CaminhoClienteResponse> caminhos = service.listarCaminhosDoCliente(clienteId);
        if (caminhos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(caminhos);
    }

    @GetMapping("/por-caminho")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista os documentos ativos de um caminho de armazenamento específico")
    public ResponseEntity<List<DocumentoResponse>> listarDocumentosPorCaminho(
            @RequestParam @NotNull(message = "O ID do caminho de armazenamento é obrigatório") @Positive(message = "O ID do caminho de armazenamento deve ser um número positivo") Long caminhoDeArmazenamentoId,
            HttpServletRequest httpRequest
    ) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para listar documentos.");
        }

        List<DocumentoResponse> documentos = service.listarDocumentosPorCaminho(caminhoDeArmazenamentoId);
        if (documentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(documentos);
    }
}
