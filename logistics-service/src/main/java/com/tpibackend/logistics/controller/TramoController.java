package com.tpibackend.logistics.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.logistics.dto.request.AsignarCamionRequest;
import com.tpibackend.logistics.dto.request.RegistrarFinTramoRequest;
import com.tpibackend.logistics.dto.request.RegistrarInicioTramoRequest;
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
@RequestMapping("/logistics/tramos")
@Validated
@Tag(name = "Tramos", description = "Gestión operativa de tramos")
@SecurityRequirement(name = "bearerAuth")
public class TramoController {

    private static final Logger log = LoggerFactory.getLogger(TramoController.class);

    private final TramoService tramoService;

    public TramoController(TramoService tramoService) {
        this.tramoService = tramoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR','TRANSPORTISTA')")
    @Operation(summary = "Listar tramos",
            description = "Obtiene los tramos operativos, permitiendo filtrar por camión asignado. Requiere rol OPERADOR o TRANSPORTISTA.")
    public ResponseEntity<java.util.List<TramoResponse>> listar(@RequestParam(required = false) Long camionId) {
        return ResponseEntity.ok(tramoService.listarTramos(camionId));
    }

        @GetMapping("/camion/{camionId}")
        @PreAuthorize("hasAnyRole('OPERADOR','TRANSPORTISTA')")
        @Operation(summary = "Listar tramos por camión",
                        description = "Obtiene los tramos asignados a un camión específico. Requiere rol OPERADOR o TRANSPORTISTA.")
        public ResponseEntity<java.util.List<TramoResponse>> listarPorCamion(@PathVariable Long camionId) {
                return ResponseEntity.ok(tramoService.listarTramos(camionId));
        }

    @GetMapping("/{tramoId}")
    @PreAuthorize("hasAnyRole('OPERADOR','TRANSPORTISTA')")
    @Operation(summary = "Detalle de tramo",
            description = "Devuelve el detalle del tramo con su estado actual. Requiere rol OPERADOR o TRANSPORTISTA.")
    public ResponseEntity<TramoResponse> obtenerDetalle(@PathVariable Long tramoId) {
        return ResponseEntity.ok(tramoService.obtenerDetalle(tramoId));
    }

    @PostMapping("/{tramoId}/asignaciones")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Asignar camión a tramo",
            description = "Asigna un camión disponible a un tramo. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AsignarCamionRequest.class),
                    examples = @ExampleObject(name = "asignarCamion",
                            value = "{\n  \"camionId\": 12\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Camión asignado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TramoResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Tramo o camión no encontrado"),
                    @ApiResponse(responseCode = "409", description = "Capacidad insuficiente o estado inválido")
            })
    public ResponseEntity<TramoResponse> asignarCamion(@PathVariable Long tramoId,
            @Valid @RequestBody AsignarCamionRequest request) {
        log.debug("Asignando camión {} al tramo {}", request.camionId(), tramoId);
        return ResponseEntity.ok(tramoService.asignarCamion(tramoId, request));
    }

    @PostMapping("/{tramoId}/inicios")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','OPERADOR')")
    @Operation(summary = "Marcar inicio del tramo",
            description = "Marca el inicio efectivo del tramo asignado. Requiere rol TRANSPORTISTA.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tramo iniciado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TramoResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "400", description = "Tramo sin camión asignado"),
                    @ApiResponse(responseCode = "409", description = "Estado inválido")
            })
    public ResponseEntity<TramoResponse> iniciarTramo(@PathVariable Long tramoId,
            @RequestBody(required = false) RegistrarInicioTramoRequest request) {
                log.debug("Marcando inicio del tramo {}", tramoId);
                log.debug("Request body iniciarTramo para tramo {}: {}", tramoId, request);
                if (request != null) {
                        log.debug("Request.fechaHoraInicio: {}", request.fechaHoraInicio());
                }
        return ResponseEntity.ok(tramoService.iniciarTramo(tramoId, request));
    }

    @PostMapping("/{tramoId}/finalizaciones")
    @PreAuthorize("hasAnyRole('TRANSPORTISTA','OPERADOR')")
    @Operation(summary = "Marcar fin del tramo",
            description = "Registra la finalización del tramo en curso tomando la telemetría interna. Requiere rol TRANSPORTISTA.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tramo finalizado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TramoResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "400", description = "Tramo no iniciado"),
                    @ApiResponse(responseCode = "409", description = "Estado inválido")
            })
    public ResponseEntity<TramoResponse> finalizarTramo(@PathVariable Long tramoId,
            @RequestBody(required = false) RegistrarFinTramoRequest request) {
                log.debug("Marcando fin del tramo {}", tramoId);
                log.debug("Request body finalizarTramo para tramo {}: {}", tramoId, request);
                if (request != null) {
                        log.debug("Request.fechaHoraFin: {}", request.fechaHoraFin());
                }
        return ResponseEntity.ok(tramoService.finalizarTramo(tramoId, request));
    }

}
