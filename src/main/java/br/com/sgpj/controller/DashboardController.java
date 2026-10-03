package br.com.sgpj.controller;

import br.com.sgpj.dto.response.Dashboard.DadosIndependentesResponse;
import br.com.sgpj.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }


    @GetMapping
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista todos os dados utilizados em KPI")
    public ResponseEntity<DadosIndependentesResponse> listarDadosIndependentes(){

        DadosIndependentesResponse response = dashboardService.listarDadosIndependentes();

        return ResponseEntity.ok(response);
    }

}
