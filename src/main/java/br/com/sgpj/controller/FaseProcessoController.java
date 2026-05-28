package br.com.sgpj.controller;

import br.com.sgpj.dto.response.FaseProcessoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.FaseProcessoService;
import br.com.sgpj.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fasesProcesso")
@Tag(name = "Fases Processos")
public class FaseProcessoController {

    private final UsuarioService usuarioService;
    public final FaseProcessoService faseProcessoService;


    public FaseProcessoController(UsuarioService usuarioService, FaseProcessoService faseProcessoService) {
        this.usuarioService = usuarioService;
        this.faseProcessoService = faseProcessoService;
    }

    @GetMapping
    public ResponseEntity<List<FaseProcessoResponse>> listarFases(HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de fases de processo.");
        }

        List<FaseProcessoResponse> fases = faseProcessoService.listarFases();

        return ResponseEntity.ok(fases);
    }
}
