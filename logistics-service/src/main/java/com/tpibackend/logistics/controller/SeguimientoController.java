package com.tpibackend.logistics.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.logistics.dto.response.ContenedorPendienteResponse;
import com.tpibackend.logistics.dto.response.SeguimientoContenedorResponse;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.service.SeguimientoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/logistics/seguimiento")
@Validated
@Tag(name = "Seguimiento", description = "Consultas de tracking para operadores y clientes")
@SecurityRequirement(name = "bearerAuth")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @GetMapping(value = "/pendientes", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Contenedores pendientes de entrega",
            description = "Devuelve el estado operativo de todos los contenedores que aún no completaron su ruta.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Consulta realizada",
                            content = @Content(array = @ArraySchema(
                                    schema = @Schema(implementation = ContenedorPendienteResponse.class)))),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido")
            })
    public ResponseEntity<List<ContenedorPendienteResponse>> obtenerPendientes(
            @RequestParam(required = false) Long solicitudId,
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) TramoEstado estadoTramo) {
        return ResponseEntity.ok(
                seguimientoService.obtenerContenedoresPendientes(solicitudId, depositoId, estadoTramo));
    }

    @GetMapping(value = "/contenedores/{contenedorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Seguimiento detallado del contenedor",
            description = "Expone la posición y el tramo actual para el contenedor indicado. Requiere especificar la solicitud que lo contiene.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Seguimiento recuperado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SeguimientoContenedorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido"),
                    @ApiResponse(responseCode = "404", description = "Solicitud o contenedor no encontrado")
            })
    public ResponseEntity<SeguimientoContenedorResponse> obtenerEstadoContenedor(
            @PathVariable Long contenedorId,
            @RequestParam Long solicitudId) {
        return ResponseEntity.ok(seguimientoService.obtenerEstadoContenedor(contenedorId, solicitudId));
    }
}
