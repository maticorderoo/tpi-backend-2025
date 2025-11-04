package com.tpibackend.orders.controller;

import com.tpibackend.orders.dto.request.EstimacionRequest;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    @Operation(summary = "Crear una nueva solicitud")
    public ResponseEntity<SolicitudResponseDto> crearSolicitud(@Valid @RequestBody SolicitudCreateRequest request) {
        SolicitudResponseDto response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/solicitudes/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Obtener detalle de la solicitud")
    public ResponseEntity<SolicitudResponseDto> obtenerSolicitud(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerSolicitud(id));
    }

    @GetMapping("/seguimiento/{contenedorId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtener seguimiento de una solicitud por contenedor")
    public ResponseEntity<SeguimientoResponseDto> obtenerSeguimiento(@PathVariable Long contenedorId) {
        return ResponseEntity.ok(solicitudService.obtenerSeguimientoPorContenedor(contenedorId));
    }

    @PostMapping("/solicitudes/{id}/estimacion")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Calcular estimación de costo y tiempo")
    public ResponseEntity<SolicitudResponseDto> calcularEstimacion(
        @PathVariable Long id,
        @Valid @RequestBody EstimacionRequest request
    ) {
        return ResponseEntity.ok(solicitudService.calcularEstimacion(id, request));
    }
}
