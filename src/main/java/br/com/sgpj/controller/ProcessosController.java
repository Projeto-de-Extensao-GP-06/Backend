package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.*;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.service.ProcessoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import br.com.sgpj.service.SincronizacaoStatusService;

import java.util.List;

@RestController
@Tag(name = "Processos")
public class ProcessosController {

    private final ProcessoService processoService;
    private final AuthorizationUtil authorizationUtil;
    private final SincronizacaoStatusService sincronizacaoStatusService;

    public ProcessosController(ProcessoService processoService,
                               AuthorizationUtil authorizationUtil,
                               SincronizacaoStatusService sincronizacaoStatusService) {
        this.processoService = processoService;
        this.authorizationUtil = authorizationUtil;
        this.sincronizacaoStatusService = sincronizacaoStatusService;
    }

    @PostMapping("/processos")
    @PreAuthorize("hasAuthority('CRIAR PROCESSO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ProcessoAppCreateResponse> novoProcesso(@Valid @RequestBody ProcessoAppCriacaoRequest novoProcesso){
        Usuario usuarioAutenticado = authorizationUtil.getUsuarioLogado();
        ProcessoAppCreateResponse processoCriado = processoService.criarProcesso(novoProcesso, usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.CREATED).body(processoCriado);
    }

    @PutMapping("/processos/{id}")
    @PreAuthorize("hasAuthority('EDITAR PROCESSO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ProcessoAppUpdateResponse> atualizarProcesso(
            @Valid @RequestBody ProcessoAppUpdateRequest processoAtualizar,
            @PathVariable Long id
    ){
        ProcessoAppUpdateResponse processoEditado = processoService.atualizarProcesso(id, processoAtualizar);
        return ResponseEntity.ok(processoEditado);
    }

    @PostMapping("/processos/{id}/atualizar-dados")
    @PreAuthorize("hasAuthority('EDITAR PROCESSO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<SincronizacaoProcessoResponse> atualizarDadosOficiais(@PathVariable Long id) {
        SincronizacaoProcessoResponse response = processoService.solicitarSincronizacaoDataJud(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping(value = "/processos/{id}/sincronizacao-status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter statusSincronizacaoEmTempoReal(@PathVariable Long id) {
        return sincronizacaoStatusService.criarConexao(id);
    }

    @DeleteMapping("/processos/{id}")
    @PreAuthorize("hasAuthority('EXCLUIR PROCESSO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Void> deletarProcesso(@PathVariable Long id){
        processoService.deletarProcesso(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/processos")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Page<ProcessoAppMiniResponse>> listarProcessos(
            @PageableDefault(size = 20, sort = "ultimaAtualizacao", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long faseId,
            @RequestParam(required = false) Long tribunalId,
            @RequestParam(required = false) Long prioridadeId,
            @RequestParam(required = false) String audiencia
    ) {
        Page<ProcessoAppMiniResponse> processos = processoService.listarProcessos(
                pageable, query, status, faseId, tribunalId, prioridadeId, audiencia);
        return ResponseEntity.ok(processos);
    }

    public ResponseEntity<Page<ProcessoAppMiniResponse>> listarProcessos(Pageable pageable) {
        return listarProcessos(pageable, null, null, null, null, null, null);
    }

    @GetMapping("/processos/{id}")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<ProcessoAppResponse> buscarProcessoPorId(@PathVariable Long id){
        ProcessoAppResponse processo = processoService.buscarPorId(id);
        return ResponseEntity.ok(processo);
    }

    @GetMapping("/clientes/{clienteId}/processos")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ProcessoAppMiniResponse>> buscarPorClienteId(@PathVariable Long clienteId){
        List<ProcessoAppMiniResponse> processos = processoService.buscarPorClienteId(clienteId);
        return ResponseEntity.ok(processos);
    }

    @GetMapping("/processos/buscaFiltro")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<ProcessoAppMiniResponse>> buscaFiltro(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long faseId,
            @RequestParam(required = false) Long tribunalId
    ) {
        List<ProcessoAppMiniResponse> processos = processoService.buscaFiltro(query, faseId, tribunalId);
        return ResponseEntity.ok(processos);
    }

    @PostMapping("/processos/{processoId}/documentos")
    @PreAuthorize("hasAuthority('CRIAR DOCUMENTO') || hasAuthority('TODAS AS PERMISSÕES')")
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

    @DeleteMapping("/processos/{processoId}/documentos/{documentoId}")
    @PreAuthorize("hasAuthority('EXCLUIR DOCUMENTO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Void> desvincularDocumento(
            @PathVariable Long processoId,
            @PathVariable Long documentoId
    ) {
        processoService.desvincularDocumento(processoId, documentoId);
        return ResponseEntity.noContent().build();
    }
}
