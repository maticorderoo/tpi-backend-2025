package com.tpibackend.fleet.controller;

import com.tpibackend.fleet.model.dto.FleetMetricsResponse;
import com.tpibackend.fleet.service.FleetMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
@Tag(name = "Métricas")
public class MetricsController {

    private final FleetMetricsService fleetMetricsService;

    public MetricsController(FleetMetricsService fleetMetricsService) {
        this.fleetMetricsService = fleetMetricsService;
    }

    @GetMapping("/promedios")
    @Operation(summary = "Promedios de consumo y costo", description = "Calcula los promedios de consumo y costo por km para camiones disponibles")
    public FleetMetricsResponse obtenerPromedios() {
        return fleetMetricsService.obtenerPromedios();
    }
}
