package com.tpibackend.fleet.controller;

import com.tpibackend.fleet.model.dto.FleetMetricsResponse;
import com.tpibackend.fleet.service.FleetMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
@Tag(name = "Métricas")
@SecurityRequirement(name = "bearerAuth")
public class MetricsController {

    private final FleetMetricsService fleetMetricsService;

    public MetricsController(FleetMetricsService fleetMetricsService) {
        this.fleetMetricsService = fleetMetricsService;
    }

    @GetMapping("/promedios")
    @Operation(summary = "Promedios de consumo y costo", description = "Calcula los promedios de consumo y costo por km para camiones disponibles",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promedios calculados",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = FleetMetricsResponse.class),
                                    examples = @ExampleObject(name = "metrics",
                                            value = "{\n  \"consumoPromedio\": 0.31,\n  \"costoKmPromedio\": 940\n}")))
            })
    public FleetMetricsResponse obtenerPromedios() {
        return fleetMetricsService.obtenerPromedios();
    }
}
