package br.com.sgpj.controller;

import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.OneDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api-onedrive")
@Tag(name = "OneDrive")
@Validated
public class OneDriveController {

    private final OneDriveService service;

    public OneDriveController(OneDriveService service) {
        this.service = service;
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista arquivos dentro de uma pasta")
    public ResponseEntity<List<OneDriveItemResponse>> listarArquivosDePasta(
            @RequestParam @NotNull(message = "O ID do caminho de armazenamento é obrigatório") @Positive(message = "O ID do caminho de armazenamento deve ser um número positivo") Long caminhoDeArmazenamentoId,
            HttpServletRequest httpRequest
    ) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para listar arquivos.");
        }

        List<OneDriveItemResponse> arquivos = service.listarArquivos(caminhoDeArmazenamentoId);
        if (arquivos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(arquivos);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Faz upload de um arquivo para a pasta do processo no OneDrive")
    public ResponseEntity<OneDriveItemResponse> uploadArquivo(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam @NotNull(message = "O ID do tipo de documento é obrigatório") @Positive(message = "O ID do tipo de documento deve ser um número positivo") Long tipoDocumentoId,
            @RequestParam @NotNull(message = "O ID do caminho de armazenamento é obrigatório") @Positive(message = "O ID do caminho de armazenamento deve ser um número positivo") Long caminhoDeArmazenamentoId,
            @RequestParam @Positive(message = "O ID do processo deve ser um número positivo") Long processoAppId,
            HttpServletRequest httpRequest
    ) throws IOException {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para upload de arquivos.");
        }

        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            throw new IllegalArgumentException("O nome do arquivo não pode ser vazio.");
        }
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }
        byte[] conteudo = arquivo.getBytes();

        OneDriveItemResponse resposta = service.uploadDocumento(nomeArquivo, conteudo, tipoDocumentoId, caminhoDeArmazenamentoId, processoAppId);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/pasta")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Cria a pasta do processo no OneDrive (nomeCliente - Cpf ou Cnpj/numero_cnj) e vincula ao cliente")
    public ResponseEntity<OneDriveItemResponse> criarPasta(
            @RequestParam @NotNull(message = "O ID do cliente é obrigatório") @Positive(message = "O ID do cliente deve ser um número positivo") Long clienteId,
            @RequestParam @NotBlank(message = "O número CNJ é obrigatório") String numeroCnj,
            HttpServletRequest httpRequest
    ) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para criar pastas.");
        }

        OneDriveItemResponse resposta = service.criarPasta(clienteId, numeroCnj);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/arquivo/renomear")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Renomeia um arquivo na pasta do processo")
    public ResponseEntity<OneDriveItemResponse> renomearArquivo(
            @RequestParam @NotNull(message = "O ID do caminho de armazenamento é obrigatório") @Positive(message = "O ID do caminho de armazenamento deve ser um número positivo") Long caminhoDeArmazenamentoId,
            @RequestParam @NotBlank(message = "O nome do arquivo é obrigatório") String nomeArquivo,
            @RequestParam @NotBlank(message = "O novo nome do arquivo é obrigatório") String novoNome,
            HttpServletRequest httpRequest
    ) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para renomear arquivos.");
        }

        OneDriveItemResponse resposta = service.renomearArquivo(caminhoDeArmazenamentoId, nomeArquivo, novoNome);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/arquivo")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Exclui um arquivo do OneDrive")
    public ResponseEntity<Void> excluirArquivo(
            @RequestParam @NotNull(message = "O ID do caminho de armazenamento é obrigatório") @Positive(message = "O ID do caminho de armazenamento deve ser um número positivo") Long caminhoDeArmazenamentoId,
            @RequestParam @NotBlank(message = "O nome do arquivo é obrigatório") String nomeArquivo,
            HttpServletRequest httpRequest
    ) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para excluir arquivos.");
        }

        service.excluirArquivo(caminhoDeArmazenamentoId, nomeArquivo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/pasta")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Exclui uma pasta (e todo seu conteúdo) do OneDrive")
    public ResponseEntity<Void> excluirPasta(
            @RequestParam @NotNull(message = "O ID do caminho de armazenamento é obrigatório") @Positive(message = "O ID do caminho de armazenamento deve ser um número positivo") Long caminhoDeArmazenamentoId,
            HttpServletRequest httpRequest
    ) {
        if (!service.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para excluir pastas.");
        }

        service.excluirPasta(caminhoDeArmazenamentoId);
        return ResponseEntity.noContent().build();
    }
}
