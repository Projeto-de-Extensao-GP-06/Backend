package br.com.sgpj.controller;

import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.service.HistoricoProcessoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Histórico")
public class HistoricoProcessoController {

    public final HistoricoProcessoService historicoProcessoService;

    public HistoricoProcessoController(HistoricoProcessoService historicoProcessoService) {
        this.historicoProcessoService = historicoProcessoService;
    }

    @PostMapping("/historico")
    public ResponseEntity<HistoricoProcessoResponse> interacaoProcesso(@Valid @RequestBody HistoricoProcessoRequest novaInteracao){

        HistoricoProcessoResponse interacaoRegistrada = historicoProcessoService.adicionarRegistro(novaInteracao);

        return ResponseEntity.status(HttpStatus.CREATED).body(interacaoRegistrada);
    }

    @GetMapping("/processo/{id}/historico")
    public ResponseEntity<List<HistoricoProcessoResponse>> buscarPorIdProcesso(@PathVariable Long id){
        List<HistoricoProcessoResponse> historicoProcesso = historicoProcessoService.buscarPorIdProcesso(id);

        return ResponseEntity.ok(historicoProcesso);
    }

    @DeleteMapping("/historico")
    public ResponseEntity<Void> deletarPorIdProcesso(Long id){

        historicoProcessoService.deletarPorIdProcesso(id);

        return ResponseEntity.noContent().build();

    }

}
