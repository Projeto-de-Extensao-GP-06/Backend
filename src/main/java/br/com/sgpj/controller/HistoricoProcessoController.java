package br.com.sgpj.controller;

import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.service.HistoricoProcessoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Histórico")
public class HistoricoProcessoController {

    private final HistoricoProcessoService historicoProcessoService;

    public HistoricoProcessoController(HistoricoProcessoService historicoProcessoService) {
        this.historicoProcessoService = historicoProcessoService;
    }

    @PostMapping("/historico")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('EDITAR PROCESSO') || hasAuthority('TODAS AS PERMISSÕES')")
    public ResponseEntity<HistoricoProcessoResponse> interacaoProcesso(@Valid @RequestBody HistoricoProcessoRequest novaInteracao){
        HistoricoProcessoResponse interacaoRegistrada = historicoProcessoService.adicionarRegistro(novaInteracao);
        return ResponseEntity.status(HttpStatus.CREATED).body(interacaoRegistrada);
    }

    @SecurityRequirement(name = "Bearer")
    @GetMapping("/processo/{id}/historico")
    public ResponseEntity<List<HistoricoProcessoResponse>> buscarPorIdProcesso(@PathVariable Long id){
        List<HistoricoProcessoResponse> historicoProcesso = historicoProcessoService.buscarPorIdProcesso(id);
        return ResponseEntity.ok(historicoProcesso);
    }

    @SecurityRequirement(name = "Bearer")
    @DeleteMapping("/historico")
    @PreAuthorize("hasAuthority('EDITAR PROCESSO') || hasAuthority('TODAS AS PERMISSÕES')")
    public ResponseEntity<Void> deletarPorIdProcesso(Long id){
        historicoProcessoService.deletarPorIdProcesso(id);
        return ResponseEntity.noContent().build();
    }
}
