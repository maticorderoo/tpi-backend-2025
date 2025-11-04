package com.tpibackend.logistics.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.logistics.dto.request.AsignarRutaRequest;
import com.tpibackend.logistics.dto.request.CrearRutaRequest;
import com.tpibackend.logistics.dto.response.RutaResponse;
import com.tpibackend.logistics.service.RutaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/logistics/rutas")
@Validated
public class RutaController {

    private static final Logger log = LoggerFactory.getLogger(RutaController.class);

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @PostMapping
    public ResponseEntity<RutaResponse> crearRuta(@Valid @RequestBody CrearRutaRequest request) {
        log.debug("Creando nueva ruta sugerida");
        RutaResponse response = rutaService.crearRuta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{rutaId}/asignar")
    public ResponseEntity<RutaResponse> asignarRuta(@PathVariable Long rutaId,
            @Valid @RequestBody AsignarRutaRequest request) {
        log.debug("Asignando ruta {} a solicitud {}", rutaId, request.solicitudId());
        RutaResponse response = rutaService.asignarRuta(rutaId, request);
        return ResponseEntity.ok(response);
    }
}
