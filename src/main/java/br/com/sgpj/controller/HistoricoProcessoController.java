package br.com.sgpj.controller;

import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.HistoricoProcessoService;
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
@Tag(name = "Histórico")
public class HistoricoProcessoController {

    private final UsuarioService usuarioService;
    public final HistoricoProcessoService historicoProcessoService;

    public HistoricoProcessoController(UsuarioService usuarioService, HistoricoProcessoService historicoProcessoService) {
        this.usuarioService = usuarioService;
        this.historicoProcessoService = historicoProcessoService;
    }

    @SecurityRequirement(name = "Bearer")
    @PostMapping("/historico")
    public ResponseEntity<HistoricoProcessoResponse> interacaoProcesso(@Valid @RequestBody HistoricoProcessoRequest novaInteracao, HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para criação de histórico.");
        }


        HistoricoProcessoResponse interacaoRegistrada = historicoProcessoService.adicionarRegistro(novaInteracao);

        return ResponseEntity.status(HttpStatus.CREATED).body(interacaoRegistrada);
    }

    @SecurityRequirement(name = "Bearer")
    @GetMapping("/processo/{id}/historico")
    public ResponseEntity<List<HistoricoProcessoResponse>> buscarPorIdProcesso(@PathVariable Long id, HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de histórico.");
        }

        List<HistoricoProcessoResponse> historicoProcesso = historicoProcessoService.buscarPorIdProcesso(id);

        return ResponseEntity.ok(historicoProcesso);
    }

    @SecurityRequirement(name = "Bearer")
    @DeleteMapping("/historico")
    public ResponseEntity<Void> deletarPorIdProcesso(Long id, HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para deleção de histórico.");
        }

        historicoProcessoService.deletarPorIdProcesso(id);

        return ResponseEntity.noContent().build();

    }

}
