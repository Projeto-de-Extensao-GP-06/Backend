package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.*;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.ProcessoService;
import br.com.sgpj.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Processos")
public class ProcessosController {

    public final ProcessoService processoService;
    private final UsuarioService usuarioService;


    public ProcessosController(ProcessoService processoService, UsuarioService usuarioService) {
        this.processoService = processoService;
        this.usuarioService = usuarioService;
    }


    @PostMapping("/processos")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ProcessoAppCreateResponse> novoProcesso(@Valid @RequestBody ProcessoAppCriacaoRequest novoProcesso, HttpServletRequest httpRequest){


        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para criação de processo.");
        }

        ProcessoAppCreateResponse processoCriado = processoService.criarProcesso(novoProcesso);

        return ResponseEntity.status(HttpStatus.CREATED).body(processoCriado);
    }

    @PutMapping("/processos/{id}")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ProcessoAppUpdateResponse> atualizarProcesso(
            @Valid @RequestBody ProcessoAppUpdateRequest processoAtualizar,
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para atualização de processo.");
        }

        ProcessoAppUpdateResponse processoEditado = processoService.atualizarProcesso(id, processoAtualizar);

        return ResponseEntity.ok(processoEditado);
    }

    @DeleteMapping("/processos/{id}")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Void> deletarProcesso(@PathVariable Long id, HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para deleção de processo.");
        }

        processoService.deletarProcesso(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/processos")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ProcessoAppMiniResponse>> listarProcessos(HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de processos.");
        }

        List<ProcessoAppMiniResponse> processos = processoService.listarProcessos();

        return ResponseEntity.ok(processos);
    }

    @GetMapping("/processos/{id}")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ProcessoAppResponse> buscarProcessoPorId(@PathVariable Long id, HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de processos.");
        }

        ProcessoAppResponse processo = processoService.buscarPorId(id);

        return ResponseEntity.ok(processo);
    }

    @GetMapping("/clientes/{clienteId}/processos")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ProcessoAppMiniResponse>> buscarPorClienteId(@PathVariable Long clienteId, HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de processos.");
        }

        List<ProcessoAppMiniResponse> processos = processoService.buscarPorClienteId(clienteId);

        return ResponseEntity.ok(processos);
    }

    @GetMapping("/processos/buscaFiltro")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ProcessoAppMiniResponse>> buscaFiltro(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long faseId,
            @RequestParam(required = false) Long tribunalId,
            HttpServletRequest httpRequest) {

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de processos.");
        }

        List<ProcessoAppMiniResponse> processos = processoService.buscaFiltro(query, faseId, tribunalId);

        return ResponseEntity.ok(processos);
    }

    @PostMapping("/processos/{processoId}/documentos")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ProcessoDocumentoResponse> anexarDocumento(
            @PathVariable Long processoId,
            @RequestParam Long documentoId,
            @RequestParam Long usuarioId
    ) {
        ProcessoDocumentoResponse response = processoService.anexarDocumento(processoId, documentoId, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/processos/{processoId}/documentos")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ProcessoDocumentoResponse>> listarDocumentosDoProcesso(
            @PathVariable Long processoId
    ) {
        List<ProcessoDocumentoResponse> docs = processoService.listarDocumentosDoProcesso(processoId);
        if (docs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(docs);
    }


}
