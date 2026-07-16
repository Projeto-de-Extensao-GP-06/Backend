package br.com.sgpj.controller;

import br.com.sgpj.dto.response.FaseProcessoResponse;
import br.com.sgpj.service.FaseProcessoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fasesProcesso")
@RequiredArgsConstructor
@Tag(name = "Fases Processos")
public class FaseProcessoController {

    public final FaseProcessoService faseProcessoService;

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<FaseProcessoResponse>> listarFases(){
        List<FaseProcessoResponse> fases = faseProcessoService.listarFases();
        return ResponseEntity.ok(fases);
    }
}
