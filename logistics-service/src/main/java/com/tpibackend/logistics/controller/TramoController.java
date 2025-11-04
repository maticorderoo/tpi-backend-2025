package com.tpibackend.logistics.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/logistics/tramos")
@Validated
public class TramoController {

    private static final Logger log = LoggerFactory.getLogger(TramoController.class);

    private final TramoService tramoService;

    public TramoController(TramoService tramoService) {
        this.tramoService = tramoService;
    }

    @PostMapping("/{tramoId}/asignar-camion")
    public ResponseEntity<TramoResponse> asignarCamion(@PathVariable Long tramoId,
            @Valid @RequestBody AsignarCamionRequest request) {
        log.debug("Asignando camión {} al tramo {}", request.camionId(), tramoId);
        return ResponseEntity.ok(tramoService.asignarCamion(tramoId, request));
    }

    @PostMapping("/{tramoId}/inicio")
    public ResponseEntity<TramoResponse> iniciarTramo(@PathVariable Long tramoId,
            @Valid @RequestBody(required = false) InicioTramoRequest request) {
        log.debug("Marcando inicio del tramo {}", tramoId);
        return ResponseEntity.ok(tramoService.iniciarTramo(tramoId, request));
    }

    @PostMapping("/{tramoId}/fin")
    public ResponseEntity<TramoResponse> finalizarTramo(@PathVariable Long tramoId,
            @Valid @RequestBody FinTramoRequest request) {
        log.debug("Marcando fin del tramo {}", tramoId);
        return ResponseEntity.ok(tramoService.finalizarTramo(tramoId, request));
    }
}
