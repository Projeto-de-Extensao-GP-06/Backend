package br.com.sgpj.controller;

import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoCreateResponse;
import br.com.sgpj.service.HistoricoProcessoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/historico")
@Tag(name = "Histórico")
public class HistoricoProcessoController {

    public final HistoricoProcessoService historicoProcessoService;

    public HistoricoProcessoController(HistoricoProcessoService historicoProcessoService) {
        this.historicoProcessoService = historicoProcessoService;
    }

    @PostMapping
    public ResponseEntity<HistoricoProcessoCreateResponse> interacaoProcesso(@Valid @RequestBody HistoricoProcessoRequest novaInteracao){

        HistoricoProcessoCreateResponse interacaoRegistrada = historicoProcessoService.adicionarRegistro(novaInteracao);

        return ResponseEntity.status(201).body(interacaoRegistrada);
    }

}
