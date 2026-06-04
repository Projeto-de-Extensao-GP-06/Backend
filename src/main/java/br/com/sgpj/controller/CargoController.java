package br.com.sgpj.controller;

import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.service.CargoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cargos")
@Tag(name = "Cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Page<CargoResponse>> listarCargosPaginados(@ParameterObject @PageableDefault(
            size = 10,
            page = 0,
            sort = "nome",
            direction = Sort.Direction.ASC
    ) Pageable pageable) {
        return ResponseEntity.ok(cargoService.listarPaginado(pageable));
    }
}
