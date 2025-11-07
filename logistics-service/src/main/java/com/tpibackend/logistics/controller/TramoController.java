package com.tpibackend.logistics.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.logistics.dto.request.AsignarCamionRequest;
import com.tpibackend.logistics.dto.request.FinTramoRequest;
import com.tpibackend.logistics.dto.request.InicioTramoRequest;
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.service.TramoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/logistics/tramos")
@Validated
@Tag(name = "Tramos", description = "Gestión operativa de tramos")
@SecurityRequirement(name = "bearerAuth")
public class TramoController {

    private static final Logger log = LoggerFactory.getLogger(TramoController.class);

    private final TramoService tramoService;

    public TramoController(TramoService tramoService) {
        this.tramoService = tramoService;
    }

    @PostMapping("/{tramoId}/asignar-camion")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Asignar camión a tramo",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AsignarCamionRequest.class),
                    examples = @ExampleObject(name = "asignarCamion",
                            value = "{\n  \"camionId\": 12,\n  \"pesoCarga\": 1200,\n  \"volumenCarga\": 28\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Camión asignado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TramoResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Tramo o camión no encontrado"),
                    @ApiResponse(responseCode = "409", description = "Capacidad insuficiente o estado inválido")
            })
    public ResponseEntity<TramoResponse> asignarCamion(@PathVariable Long tramoId,
            @Valid @RequestBody AsignarCamionRequest request) {
        log.debug("Asignando camión {} al tramo {}", request.camionId(), tramoId);
        return ResponseEntity.ok(tramoService.asignarCamion(tramoId, request));
    }

    @PostMapping("/{tramoId}/inicio")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    @Operation(summary = "Marcar inicio del tramo",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = InicioTramoRequest.class),
                    examples = @ExampleObject(name = "inicioTramo",
                            value = "{\n  \"fechaHoraInicio\": \"2024-06-15T08:30:00Z\"\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tramo iniciado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TramoResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Tramo sin camión asignado"),
                    @ApiResponse(responseCode = "409", description = "Estado inválido")
            })
    public ResponseEntity<TramoResponse> iniciarTramo(@PathVariable Long tramoId,
            @Valid @RequestBody(required = false) InicioTramoRequest request) {
        log.debug("Marcando inicio del tramo {}", tramoId);
        return ResponseEntity.ok(tramoService.iniciarTramo(tramoId, request));
    }

    @PostMapping("/{tramoId}/fin")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    @Operation(summary = "Marcar fin del tramo",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = FinTramoRequest.class),
                    examples = @ExampleObject(name = "finTramo",
                            value = "{\n  \"fechaHoraFin\": \"2024-06-15T17:45:00Z\",\n  \"kmReal\": 318.2,\n  \"consumoLitrosKm\": 0.35,\n  \"precioCombustible\": 750,\n  \"costoKmBase\": 950,\n  \"diasEstadia\": 1\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tramo finalizado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TramoResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Tramo no iniciado"),
                    @ApiResponse(responseCode = "409", description = "Estado inválido")
            })
    public ResponseEntity<TramoResponse> finalizarTramo(@PathVariable Long tramoId,
            @Valid @RequestBody FinTramoRequest request) {
        log.debug("Marcando fin del tramo {}", tramoId);
        return ResponseEntity.ok(tramoService.finalizarTramo(tramoId, request));
    }
}
