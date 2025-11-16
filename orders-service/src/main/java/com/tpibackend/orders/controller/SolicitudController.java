package com.tpibackend.orders.controller;

import com.tpibackend.orders.dto.request.ContenedorEstadoUpdateRequest;
import com.tpibackend.orders.dto.request.EstimacionRequest;
import com.tpibackend.orders.dto.request.SolicitudCostoUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.request.SolicitudEstadoUpdateRequest;
import com.tpibackend.orders.dto.response.ContenedorResponseDto;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.service.ContenedorService;
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
@RequestMapping("/orders")
@Validated
@Tag(name = "Solicitudes", description = "Gesti\u00f3n de solicitudes de transporte")
@SecurityRequirement(name = "bearerAuth")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final ContenedorService contenedorService;

    public SolicitudController(SolicitudService solicitudService, ContenedorService contenedorService) {
        this.solicitudService = solicitudService;
        this.contenedorService = contenedorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Crear una nueva solicitud",
            description = "Registra una solicitud nueva creando el cliente y el contenedor en caso de que no existan. Requiere rol CLIENTE.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SolicitudCreateRequest.class),
                    examples = @ExampleObject(name = "crearSolicitud",
                            summary = "Solicitud de traslado b\u00e1sica",
                            value = "{\n  \"cliente\": {\n    \"nombre\": \"ACME Corp\",\n    \"email\": \"contacto@acme.com\",\n    \"telefono\": \"+54 11 5555-1111\"\n  },\n  \"contenedor\": {\n    \"peso\": 1200.5,\n    \"volumen\": 28.4\n  },\n  \"origen\": \"Buenos Aires\",\n  \"destino\": \"C\u00f3rdoba\",\n  \"estadiaEstimada\": 2.5\n}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Solicitud creada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class),
                                    examples = @ExampleObject(name = "solicitudCreada",
                                            summary = "Solicitud creada correctamente",
                                            value = "{\n  \"id\": 42,\n  \"costoEstimado\": 150000,\n  \"tiempoEstimadoMinutos\": 720,\n  \"costoFinal\": null,\n  \"tiempoRealMinutos\": null,\n  \"estadiaEstimada\": 2.5,\n  \"fechaCreacion\": \"2025-11-16T14:30:00Z\",\n  \"cliente\": {\n    \"id\": 15,\n    \"nombre\": \"ACME Corp\"\n  },\n  \"contenedor\": {\n    \"id\": 9,\n    \"codigo\": \"CONT-0009\",\n    \"peso\": 1200.5,\n    \"volumen\": 28.4,\n    \"estado\": \"BORRADOR\"\n  },\n  \"eventos\": [],\n  \"rutaResumen\": null\n}"))),
                    @ApiResponse(responseCode = "400", description = "Datos inv\u00e1lidos"),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}")))
            }
    )
    public ResponseEntity<SolicitudResponseDto> crearSolicitud(@Valid @RequestBody SolicitudCreateRequest request) {
        SolicitudResponseDto response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Obtener detalle de la solicitud",
            description = "Recupera el detalle de una solicitud existente. Requiere rol CLIENTE u OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud encontrada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class),
                                    examples = @ExampleObject(name = "solicitudDetalle",
                                            value = "{\n  \"id\": 42,\n  \"costoEstimado\": 150000,\n  \"tiempoEstimadoMinutos\": 720,\n  \"costoFinal\": 185000,\n  \"tiempoRealMinutos\": 810,\n  \"estadiaEstimada\": 2.5,\n  \"fechaCreacion\": \"2025-11-16T14:30:00Z\",\n  \"cliente\": {\n    \"id\": 15,\n    \"nombre\": \"ACME Corp\"\n  },\n  \"contenedor\": {\n    \"id\": 9,\n    \"codigo\": \"CONT-0009\",\n    \"peso\": 1200.5,\n    \"volumen\": 28.4,\n    \"estado\": \"PROGRAMADA\"\n  },\n  \"eventos\": [\n    {\n      \"estado\": \"PROGRAMADA\",\n      \"fechaEvento\": \"2025-11-16T15:00:00Z\",\n      \"descripcion\": \"Solicitud programada\"\n    },\n    {\n      \"estado\": \"COMPLETADA\",\n      \"fechaEvento\": \"2025-11-17T03:00:00Z\",\n      \"descripcion\": \"Entrega completada\"\n    }\n  ],\n  \"rutaResumen\": {\n    \"rutaId\": 21,\n    \"estado\": \"ASIGNADA\",\n    \"tramos\": 3\n  }\n}"))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
            })
    public ResponseEntity<SolicitudResponseDto> obtenerSolicitud(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerSolicitud(id));
    }

    @GetMapping("/{id}/tracking")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Obtener seguimiento de una solicitud por contenedor",
            description = "Devuelve el estado y eventos asociados a una solicitud por contenedor. Requiere rol CLIENTE u OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Seguimiento recuperado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SeguimientoResponseDto.class),
                                    examples = @ExampleObject(name = "seguimiento",
                                            value = "{\n  \"contenedorId\": 9,\n  \"estadoActual\": \"EN_TRANSITO\",\n  \"eventos\": [\n    {\n      \"estado\": \"PROGRAMADA\",\n      \"descripcion\": \"Solicitud programada\"\n    },\n    {\n      \"estado\": \"EN_TRANSITO\",\n      \"descripcion\": \"Camión asignado y en curso\"\n    }\n  ]\n}"))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
            })
    public ResponseEntity<SeguimientoResponseDto> obtenerSeguimiento(@PathVariable("id") Long id) {
        return ResponseEntity.ok(solicitudService.obtenerSeguimientoPorContenedor(id));
    }

    @PostMapping("/{id}/estimacion")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
            summary = "Calcular estimación de costo y tiempo",
            description = "Calcula el costo y tiempo estimado utilizando distance-client y métricas de flota. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EstimacionRequest.class),
                    examples = @ExampleObject(name = "estimacion",
                            value = "{\n  \"combustiblePrecioLitro\": 750,\n  \"tiempoCargaHoras\": 4\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estimación calculada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
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

    @PutMapping("/{id}/costo")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar costo final de una solicitud",
            description = "Actualiza el costo y tiempo real de la solicitud. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SolicitudCostoUpdateRequest.class),
                    examples = @ExampleObject(name = "actualizarCosto",
                            value = "{\n  \"costoFinal\": 185000,\n  \"tiempoRealMinutos\": 810\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Costo actualizado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
                    @ApiResponse(responseCode = "409", description = "Estado inválido para cierre")
            })
    public ResponseEntity<SolicitudResponseDto> actualizarCosto(
        @PathVariable Long id,
        @Valid @RequestBody SolicitudCostoUpdateRequest request
    ) {
        return ResponseEntity.ok(solicitudService.actualizarCosto(id, request));
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(
        summary = "Actualizar manualmente el estado del contenedor",
        description = "Permite a un operador cambiar manualmente el estado del contenedor asociado a una solicitud. Solo disponible para rol OPERADOR.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ContenedorResponseDto.class),
                    examples = @ExampleObject(
                        name = "contenedorActualizado",
                        value = "{\n  \"id\": 9,\n  \"codigo\": \"CONT-0009\",\n  \"peso\": 1200.5,\n  \"volumen\": 28.4,\n  \"estado\": \"EN_VIAJE\"\n}"
                    ))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso prohibido - Se requiere rol OPERADOR"),
            @ApiResponse(responseCode = "404", description = "Solicitud o contenedor no encontrado")
        }
    )
    public ResponseEntity<ContenedorResponseDto> actualizarEstadoContenedor(
        @PathVariable Long id,
        @Valid @RequestBody ContenedorEstadoUpdateRequest request
    ) {
        return ResponseEntity.ok(contenedorService.actualizarEstadoManual(id, request));
    }
}
