package br.com.sgpj.controller;

import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.request.UsuarioCriacaoRequest;
import br.com.sgpj.dto.response.UsuarioListarResponse;
import br.com.sgpj.service.UsuarioService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários")
@SecurityRequirement(name = "Bearer")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(

            UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }

    @PostMapping
    @PreAuthorize(
            "hasAuthority('CRIAR USUÁRIO') || hasAuthority('TODAS AS PERMISSÕES')"
    )
    public ResponseEntity<Void> criar(
            @RequestBody @Valid UsuarioCriacaoRequest request
    ) {
        usuarioService.criar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping
    public ResponseEntity<List<UsuarioListarResponse>> listarTodos() {

        List<UsuarioListarResponse> usuarios =
                usuarioService.listarTodos();

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/cargo")
    public ResponseEntity<List<UsuarioListarResponse>> listarPorCargo(
            @RequestParam String cargo
    ) {

        List<UsuarioListarResponse> usuarios =
                usuarioService.listarPorCargo(cargo);

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/pesquisar")
    public ResponseEntity<List<UsuarioListarResponse>> pesquisar(
            @RequestParam String pesquisa
    ) {

        List<UsuarioListarResponse> usuarios =
                usuarioService.buscarPorNomeOuEmail(pesquisa);

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('EDITAR USUÁRIO') || hasAuthority('TODAS AS PERMISSÕES')"
    )
    public ResponseEntity<UsuarioListarResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid UsuarioAtualizacaoRequest request
    ) {

        UsuarioListarResponse usuario =
                usuarioService.atualizarUsuario(id, request);

        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('EXCLUIR USUÁRIO') || hasAuthority('TODAS AS PERMISSÕES')"
    )
    public ResponseEntity<Void> deletar(
            @PathVariable Long id
    ) {

        usuarioService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reactivate/{id}")
    @PreAuthorize(
            "hasAuthority('EDITAR USUÁRIO') || hasAuthority('TODAS AS PERMISSÕES')"
    )
    public ResponseEntity<UsuarioListarResponse> reativar(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                usuarioService.reativarUsuario(id)
        );
    }
}