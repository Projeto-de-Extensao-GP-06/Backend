package br.com.sgpj.controller;

import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.CargoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cargos")
@Tag(name = "Cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista os cargos e suas permissões de forma paginada")
    public ResponseEntity<Page<CargoResponse>> listarCargosPaginados(@ParameterObject @PageableDefault(
            size = 10,
            page = 0,
            sort = "nome",
            direction = Sort.Direction.ASC
    ) Pageable pageable, HttpServletRequest httpRequest) {

        if (!cargoService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para listagem de cargos.");
        }

        return ResponseEntity.ok(cargoService.listarPaginado(pageable));
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Cria cargo com lista de permissões vazia")
    public ResponseEntity<CargoResponse> criar(@RequestBody @Valid CriarCargoRequest request,
                                               HttpServletRequest httpRequest) {
        if (!cargoService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para criação de cargo.");
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.status(201).body(cargoService.criar(request, email));
    }

    @PutMapping("/{cargoId}/permissoes")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Atualiza a lista de permissões de um cargo")
    public ResponseEntity<CargoResponse> atualizarPermissoes(
            @PathVariable Long cargoId,
            @RequestBody @Valid AtribuirPermissoesRequest request,
            HttpServletRequest httpRequest) {
        if (!cargoService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para atualização de cargo.");
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(cargoService.atualizarPermissoes(cargoId, request, email));
    }

    @DeleteMapping("/{cargoId}")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Deleta um cargo e atribui o cargo 'Sem Cargo' aos usuários associados ao cargo excluído")
    public ResponseEntity<Void> deletar(@PathVariable Long cargoId,
                                        HttpServletRequest httpRequest) {
        if (!cargoService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para deleção de cargo.");
        }

        cargoService.deletar(cargoId);
        return ResponseEntity.noContent().build();
    }
}
