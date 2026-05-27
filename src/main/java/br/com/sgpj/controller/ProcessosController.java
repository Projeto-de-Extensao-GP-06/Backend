package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppMiniResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.service.ProcessoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Processos")
public class ProcessosController {

    public final ProcessoService processoService;

    public ProcessosController(ProcessoService processoService) {
        this.processoService = processoService;
    }


    @PostMapping("/processos")
    public ResponseEntity<ProcessoAppCreateResponse> novoProcesso(@Valid @RequestBody ProcessoAppCriacaoRequest novoProcesso){

        ProcessoAppCreateResponse processoCriado = processoService.criarProcesso(novoProcesso);

        return ResponseEntity.status(HttpStatus.CREATED).body(processoCriado);
    }

    @PutMapping("/processos/{id}")
    public ResponseEntity<ProcessoAppUpdateResponse> atualizarProcesso(
            @Valid @RequestBody ProcessoAppUpdateRequest processoAtualizar,
            @PathVariable Long id
    ){
        ProcessoAppUpdateResponse processoEditado = processoService.atualizarProcesso(id, processoAtualizar);

        return ResponseEntity.ok(processoEditado);
    }

    @DeleteMapping("/processos/{id}")
    public ResponseEntity<Void> deletarProcesso(@PathVariable Long id){

        processoService.deletarProcesso(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/processos")
    public ResponseEntity<List<ProcessoAppMiniResponse>> listarProcessos(){

        List<ProcessoAppMiniResponse> processos = processoService.listarProcessos();

        return ResponseEntity.ok(processos);
    }

    @GetMapping("/processos/{id}")
    public ResponseEntity<ProcessoAppResponse> buscarProcessoPorId(@PathVariable Long id){

        ProcessoAppResponse processo = processoService.buscarPorId(id);

        return ResponseEntity.ok(processo);
    }

    @GetMapping("/clientes/{clienteId}/processos")
    public ResponseEntity<List<ProcessoAppMiniResponse>> buscarPorClienteId(@PathVariable Long clienteId){
        List<ProcessoAppMiniResponse> processos = processoService.buscarPorClienteId(clienteId);

        return ResponseEntity.ok(processos);
    }

    @GetMapping("/processos/buscaFiltro")
    public ResponseEntity<List<ProcessoAppMiniResponse>> buscaFiltro(@RequestParam(required = false) String query,
                                                                     @RequestParam(required = false) Long faseId,
                                                                     @RequestParam(required = false) Long tribunalId){
        List<ProcessoAppMiniResponse> processos = processoService.buscaFiltro(query, faseId, tribunalId);

        return ResponseEntity.ok(processos);
    }




}
