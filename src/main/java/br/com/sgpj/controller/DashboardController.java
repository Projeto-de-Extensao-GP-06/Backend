package br.com.sgpj.controller;

import br.com.sgpj.dto.response.Dashboard.DadosFiltradosDashResponse;
import br.com.sgpj.dto.response.Dashboard.DadosIndependentesResponse;
import br.com.sgpj.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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


    @GetMapping("/filter")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Faz a consulta dos dados que são utilizados para popular os gráficos da dashboard e abstração para entendimento da IA")
    public ResponseEntity<DadosFiltradosDashResponse> listarDadosFiltradosDash(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ){

        if (dateTo.isBefore(dateFrom) || dateFrom.isAfter(LocalDate.now()) || dateTo.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().build();
        }

        DadosFiltradosDashResponse respose = dashboardService.listarDadosFiltradosDash(dateFrom, dateTo);
        return ResponseEntity.ok(respose);
    }
}
