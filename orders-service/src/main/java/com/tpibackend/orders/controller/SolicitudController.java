package com.tpibackend.orders.controller;

import com.tpibackend.orders.dto.request.EstimacionRequest;
import com.tpibackend.orders.dto.request.SolicitudCostoUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.request.SolicitudEstadoUpdateRequest;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Validated
@Tag(name = "Solicitudes", description = "Gestión de solicitudes de transporte")
@SecurityRequirement(name = "bearerAuth")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(
            summary = "Crear una nueva solicitud",
            description = "Registra una solicitud nueva creando el cliente y el contenedor en caso de que no existan.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SolicitudCreateRequest.class),
                    examples = @ExampleObject(name = "crearSolicitud",
                            summary = "Solicitud de traslado básica",
                            value = "{\n  \"cliente\": {\n    \"nombre\": \"ACME Corp\",\n    \"email\": \"contacto@acme.com\",\n    \"telefono\": \"+54 11 5555-1111\"\n  },\n  \"contenedor\": {\n    \"peso\": 1200.5,\n    \"volumen\": 28.4\n  },\n  \"origen\": \"Buenos Aires\",\n  \"destino\": \"Córdoba\",\n  \"estadiaEstimada\": 2.5\n}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Solicitud creada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class),
                                    examples = @ExampleObject(name = "solicitudCreada",
                                            summary = "Solicitud creada correctamente",
                                            value = "{\n  \"id\": 42,\n  \"estado\": \"PROGRAMADA\",\n  \"costoEstimado\": 150000,\n  \"tiempoEstimadoMinutos\": 720,\n  \"origen\": \"Buenos Aires\",\n  \"destino\": \"Córdoba\",\n  \"cliente\": {\n    \"id\": 15,\n    \"nombre\": \"ACME Corp\"\n  },\n  \"contenedor\": {\n    \"id\": 9,\n    \"peso\": 1200.5,\n    \"volumen\": 28.4\n  },\n  \"eventos\": []\n}"))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos")
            }
    )
    public ResponseEntity<SolicitudResponseDto> crearSolicitud(@Valid @RequestBody SolicitudCreateRequest request) {
        SolicitudResponseDto response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/solicitudes/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Obtener detalle de la solicitud",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud encontrada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class),
                                    examples = @ExampleObject(name = "solicitudDetalle",
                                            value = "{\n  \"id\": 42,\n  \"estado\": \"EN_TRANSITO\",\n  \"rutaResumen\": {\n    \"rutaId\": 21,\n    \"estado\": \"ASIGNADA\",\n    \"tramos\": 3\n  }\n}"))),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
            })
    public ResponseEntity<SolicitudResponseDto> obtenerSolicitud(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerSolicitud(id));
    }

    @GetMapping("/seguimiento/{contenedorId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtener seguimiento de una solicitud por contenedor",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Seguimiento recuperado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SeguimientoResponseDto.class),
                                    examples = @ExampleObject(name = "seguimiento",
                                            value = "{\n  \"contenedorId\": 9,\n  \"estadoActual\": \"EN_TRANSITO\",\n  \"eventos\": [\n    {\n      \"estado\": \"PROGRAMADA\",\n      \"descripcion\": \"Solicitud programada\"\n    },\n    {\n      \"estado\": \"EN_TRANSITO\",\n      \"descripcion\": \"Camión asignado y en curso\"\n    }\n  ]\n}"))),
                    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
            })
    public ResponseEntity<SeguimientoResponseDto> obtenerSeguimiento(@PathVariable Long contenedorId) {
        return ResponseEntity.ok(solicitudService.obtenerSeguimientoPorContenedor(contenedorId));
    }

    @PostMapping("/solicitudes/{id}/estimacion")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
            summary = "Calcular estimación de costo y tiempo",
            description = "Calcula el costo y tiempo estimado utilizando distance-client y métricas de flota.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EstimacionRequest.class),
                    examples = @ExampleObject(name = "estimacion",
                            value = "{\n  \"combustiblePrecioLitro\": 750,\n  \"tiempoCargaHoras\": 4\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estimación calculada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
                    @ApiResponse(responseCode = "409", description = "Solicitud no admite estimación")
            }
    )
    public ResponseEntity<SolicitudResponseDto> calcularEstimacion(
        @PathVariable Long id,
        @Valid @RequestBody EstimacionRequest request
    ) {
        return ResponseEntity.ok(solicitudService.calcularEstimacion(id, request));
    }

    @PutMapping("/solicitudes/{id}/estado")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar estado de una solicitud",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SolicitudEstadoUpdateRequest.class),
                    examples = @ExampleObject(name = "actualizarEstado",
                            value = "{\n  \"estado\": \"EN_TRANSITO\"\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado actualizado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Transición inválida"),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
            })
    public ResponseEntity<SolicitudResponseDto> actualizarEstado(
        @PathVariable Long id,
        @Valid @RequestBody SolicitudEstadoUpdateRequest request
    ) {
        return ResponseEntity.ok(solicitudService.actualizarEstado(id, request));
    }

    @PutMapping("/solicitudes/{id}/costo")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar costo final de una solicitud",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SolicitudCostoUpdateRequest.class),
                    examples = @ExampleObject(name = "actualizarCosto",
                            value = "{\n  \"costoFinal\": 185000,\n  \"tiempoRealMinutos\": 810\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Costo actualizado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
                    @ApiResponse(responseCode = "409", description = "Estado inválido para cierre")
            })
    public ResponseEntity<SolicitudResponseDto> actualizarCosto(
        @PathVariable Long id,
        @Valid @RequestBody SolicitudCostoUpdateRequest request
    ) {
        return ResponseEntity.ok(solicitudService.actualizarCosto(id, request));
    }
}
