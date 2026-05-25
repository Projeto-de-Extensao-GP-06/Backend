package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppMiniResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.service.ProcessoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/processos")
@Tag(name = "Processos")
public class ProcessosController {

    public final ProcessoService processoService;

    public ProcessosController(ProcessoService processoService) {
        this.processoService = processoService;
    }


    @PostMapping
    public ResponseEntity<ProcessoAppCreateResponse> novoProcesso(@Valid @RequestBody ProcessoAppCriacaoRequest novoProcesso){

        ProcessoAppCreateResponse processoCriado = processoService.criarProcesso(novoProcesso);

        return ResponseEntity.status(201).body(processoCriado);
    }

    @GetMapping
    public ResponseEntity<List<ProcessoAppMiniResponse>> listarProcessos(){

        List<ProcessoAppMiniResponse> processos = processoService.listarProcessos();

        return ResponseEntity.status(200).body(processos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessoAppResponse> buscarProcessoPorId(@PathVariable Long id){

        ProcessoAppResponse processo = processoService.buscarPorId(id);

        return ResponseEntity.status(200).body(processo);
    }





}
