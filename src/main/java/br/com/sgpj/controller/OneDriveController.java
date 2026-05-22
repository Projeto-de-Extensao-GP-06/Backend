package br.com.sgpj.controller;

import br.com.sgpj.dto.OneDriveItemResponse;
import br.com.sgpj.service.OneDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api-onedrive")
@Tag(name = "OneDrive")
public class OneDriveController {

    private final OneDriveService service;

    public OneDriveController(OneDriveService service) {
        this.service = service;
    }

    @GetMapping("/{nomePasta}")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista arquivos dentro de uma pasta")
    public ResponseEntity<List<OneDriveItemResponse>> listarArquivosDePasta(@PathVariable String nomePasta) {
        List<OneDriveItemResponse> arquivos = service.listarArquivos(nomePasta);
        if (arquivos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(arquivos);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Faz upload de um arquivo para o OneDrive (suporta arquivos grandes)")
    public ResponseEntity<OneDriveItemResponse> uploadArquivo(
            @RequestParam String nomePasta,
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {

        String nomeArquivo = arquivo.getOriginalFilename();
        byte[] conteudo = arquivo.getBytes();

        OneDriveItemResponse resposta = service.uploadDocumento(nomePasta, nomeArquivo, conteudo);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{nomePasta}")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Cria uma pasta no OneDrive")
    public ResponseEntity<OneDriveItemResponse> criarPasta(@PathVariable String nomePasta) {
        OneDriveItemResponse resposta = service.criarPasta(nomePasta);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/arquivo/renomear")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Renomeia um arquivo no OneDrive")
    public ResponseEntity<OneDriveItemResponse> renomearArquivo(
            @RequestParam String nomePasta,
            @RequestParam String nomeArquivo,
            @RequestParam String novoNome) {
        OneDriveItemResponse resposta = service.renomearArquivo(nomePasta, nomeArquivo, novoNome);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/arquivo")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Exclui um arquivo do OneDrive")
    public ResponseEntity<Void> excluirArquivo(
            @RequestParam String nomePasta,
            @RequestParam String nomeArquivo) {
        service.excluirArquivo(nomePasta, nomeArquivo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{nomePasta}")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Exclui uma pasta (e todo seu conteúdo) do OneDrive")
    public ResponseEntity<Void> excluirPasta(@PathVariable String nomePasta) {
        service.excluirPasta(nomePasta);
        return ResponseEntity.noContent().build();
    }
}
