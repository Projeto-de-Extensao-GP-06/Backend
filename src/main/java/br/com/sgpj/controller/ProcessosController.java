package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.ProcessoAppResponse;
import br.com.sgpj.service.ProcessoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/processos")
@Tag(name = "Processos")
public class ProcessosController {

    public final ProcessoService processoService;

    public ProcessosController(ProcessoService processoService) {
        this.processoService = processoService;
    }


    @PostMapping
    public ResponseEntity<ProcessoAppResponse> novoProcesso(@Valid @RequestBody ProcessoAppCriacaoRequest novoProcesso){

        ProcessoAppResponse processoCriado = processoService.criarProcesso(novoProcesso);

        return ResponseEntity.status(201).body(processoCriado);
    }

//    @PutMapping
//    public ResponseEntity<ProcessoAppResponse> editarProcesso(
//            @Valid @RequestBody ProcessoAppEdicaoRequest editProcesso,
//            @PathVariable Long id){
//
//        ProcessoAppResponse processoEditado = processoService.editarProcesso(editProcesso, id);
//
//        return ResponseEntity.ok().body(processoEditado);
//    }





}
