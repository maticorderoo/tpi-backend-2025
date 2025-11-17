package com.tpibackend.logistics.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.logistics.dto.request.AsignarRutaRequest;
import com.tpibackend.logistics.dto.request.CrearRutaRequest;
import com.tpibackend.logistics.dto.request.EstimacionDistanciaRequest;
import com.tpibackend.logistics.dto.response.EstimacionDistanciaResponse;
import com.tpibackend.logistics.dto.response.RutaResponse;
import com.tpibackend.logistics.dto.response.RutaTentativaResponse;
import com.tpibackend.logistics.dto.response.TramoResponse;
import com.tpibackend.logistics.service.RutaService;
import com.tpibackend.logistics.service.RutaTentativaService;
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
@RequestMapping("/logistics/routes")
@Validated
@Tag(name = "Rutas", description = "Gestión de rutas y asignaciones")
@SecurityRequirement(name = "bearerAuth")
public class RutaController {

    private static final Logger log = LoggerFactory.getLogger(RutaController.class);

    private final RutaService rutaService;
    private final RutaTentativaService rutaTentativaService;
    private final TramoService tramoService;

    public RutaController(RutaService rutaService,
            RutaTentativaService rutaTentativaService,
            TramoService tramoService) {
        this.rutaService = rutaService;
        this.rutaTentativaService = rutaTentativaService;
        this.tramoService = tramoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
            summary = "Sugerir ruta",
            description = "Genera la ruta con tramos estimados entre un origen, depósitos intermedios y destino. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CrearRutaRequest.class),
                    examples = @ExampleObject(name = "crearRuta",
                            value = "{\n  \"origen\": {\n    \"tipo\": \"CLIENTE\",\n    \"descripcion\": \"Planta Pilar\"\n  },\n  \"destino\": {\n    \"tipo\": \"CLIENTE\",\n    \"descripcion\": \"Puerto de Rosario\"\n  },\n  \"depositosIntermedios\": [\n    {\n      \"depositoId\": 3,\n      \"diasEstadia\": 1\n    }\n  ],\n  \"costoKmBase\": 950,\n  \"consumoLitrosKm\": 0.32,\n  \"precioCombustible\": 750,\n  \"pesoCarga\": 1200,\n  \"volumenCarga\": 28\n}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Ruta sugerida",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RutaResponse.class),
                                    examples = @ExampleObject(name = "rutaSugerida",
                                            value = "{\n  \"id\": 21,\n  \"cantTramos\": 3,\n  \"costoTotalAprox\": 185000,\n  \"tramos\": [\n    {\n      \"id\": 55,\n      \"estado\": \"ESTIMADO\",\n      \"distanciaKmEstimada\": 320.5\n    }\n  ]\n}"))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "422", description = "No se pudo generar ruta")
            }
    )
    public ResponseEntity<RutaResponse> crearRuta(@Valid @RequestBody CrearRutaRequest request) {
        log.debug("Creando nueva ruta sugerida");
        RutaResponse response = rutaService.crearRuta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/estimaciones/distancia")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Calcular distancia logística",
            description = "Centraliza el uso de distance-client en Logistics. Requiere rol OPERADOR.")
    public ResponseEntity<EstimacionDistanciaResponse> estimarDistancia(
            @Valid @RequestBody EstimacionDistanciaRequest request) {
        return ResponseEntity.ok(rutaService.estimarDistancia(request.origen(), request.destino()));
    }

    @PostMapping("/{rutaId}/asignaciones")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Asignar ruta a solicitud",
            description = "Vincula una ruta planificada a una solicitud. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AsignarRutaRequest.class),
                    examples = @ExampleObject(name = "asignarRuta",
                            value = "{\n  \"solicitudId\": 42\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ruta asignada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RutaResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Ruta o solicitud no encontrada"),
                    @ApiResponse(responseCode = "409", description = "Solicitud no admite asignación")
            })
    public ResponseEntity<RutaResponse> asignarRuta(@PathVariable Long rutaId,
            @Valid @RequestBody AsignarRutaRequest request) {
        log.debug("Asignando ruta {} a solicitud {}", rutaId, request.solicitudId());
        RutaResponse response = rutaService.asignarRuta(rutaId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{rutaId}/tramos")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar tramos de una ruta confirmada",
            description = "Permite al operador ver el detalle de los tramos planificados para coordinar camiones.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado de tramos",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TramoResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
            })
    public ResponseEntity<List<TramoResponse>> listarTramosPorRuta(@PathVariable Long rutaId) {
        return ResponseEntity.ok(tramoService.listarTramosPorRuta(rutaId));
    }

    @GetMapping("/solicitudes/{solicitudId}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Obtener ruta por solicitud",
            description = "Consulta la ruta asociada a una solicitud. Requiere rol OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ruta encontrada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RutaResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Ruta no encontrada")
            })
    public ResponseEntity<RutaResponse> obtenerPorSolicitud(@PathVariable Long solicitudId) {
        return rutaService.obtenerRutaPorSolicitud(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/solicitudes/{solicitudId}/rutas-tentativas")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Generar rutas tentativas",
            description = "Devuelve todas las rutas tentativas calculadas con depósitos intermedios disponibles.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rutas generadas",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RutaTentativaResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
            })
    public ResponseEntity<List<RutaTentativaResponse>> generarTentativas(@PathVariable Long solicitudId) {
        List<RutaTentativaResponse> rutas = rutaTentativaService.generarTentativas(solicitudId);
        return ResponseEntity.ok(rutas);
    }

    @PostMapping("/solicitudes/{solicitudId}/rutas-tentativas/{rutaTentativaId}/confirmar")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Confirmar ruta tentativa",
            description = "Persiste la ruta tentativa elegida como ruta definitiva de la solicitud y actualiza Orders.")
    public ResponseEntity<RutaResponse> confirmarRutaTentativa(@PathVariable Long solicitudId,
            @PathVariable Long rutaTentativaId) {
        RutaResponse response = rutaTentativaService.confirmarTentativa(solicitudId, rutaTentativaId);
        return ResponseEntity.ok(response);
    }
}
