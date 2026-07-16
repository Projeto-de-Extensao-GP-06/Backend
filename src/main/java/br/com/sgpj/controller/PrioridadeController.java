package br.com.sgpj.controller;

import br.com.sgpj.dto.response.PrioridadeResponse;
import br.com.sgpj.service.PrioridadeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prioridades")
@Tag(name = "Prioridades")
public class PrioridadeController {

    private final PrioridadeService prioridadeService;

    public PrioridadeController(PrioridadeService prioridadeService) { this.prioridadeService = prioridadeService; }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<PrioridadeResponse>> listarPrioridades() {
        return ResponseEntity.ok(prioridadeService.listarPrioridades());
    }
}
