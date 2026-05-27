package br.com.sgpj.controller;

import br.com.sgpj.dto.response.FaseProcessoResponse;
import br.com.sgpj.service.FaseProcessoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fasesProcesso")
@Tag(name = "Fases Processos")
public class FaseProcessoController {

    public final FaseProcessoService faseProcessoService;


    public FaseProcessoController(FaseProcessoService faseProcessoService) {
        this.faseProcessoService = faseProcessoService;
    }

    @GetMapping
    public ResponseEntity<List<FaseProcessoResponse>> listarFases(){

        List<FaseProcessoResponse> fases = faseProcessoService.listarFases();

        return ResponseEntity.ok(fases);
    }
}
