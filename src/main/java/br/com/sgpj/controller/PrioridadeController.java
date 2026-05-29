package br.com.sgpj.controller;

import br.com.sgpj.dto.response.PrioridadeResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.PrioridadeService;
import br.com.sgpj.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prioridades")
@Tag(name = "Prioridades")
public class PrioridadeController {

    private final UsuarioService usuarioService;
    private final PrioridadeService prioridadeService;

    public PrioridadeController(UsuarioService usuarioService, PrioridadeService prioridadeService) {
        this.usuarioService = usuarioService;
        this.prioridadeService = prioridadeService;
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<PrioridadeResponse>> listarPrioridades(HttpServletRequest httpRequest) {

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de prioridades.");
        }


        return ResponseEntity.ok(prioridadeService.listarPrioridades());
    }
}
