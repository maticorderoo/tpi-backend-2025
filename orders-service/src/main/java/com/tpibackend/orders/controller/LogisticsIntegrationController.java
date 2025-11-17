package com.tpibackend.orders.controller;

import com.tpibackend.orders.dto.request.ContenedorEstadoUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudCostoUpdateRequest;
import com.tpibackend.orders.service.ContenedorService;
import com.tpibackend.orders.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/orders/internal/logistics")
@Validated
public class LogisticsIntegrationController {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final ContenedorService contenedorService;
    private final SolicitudService solicitudService;
    private final String sharedSecret;

    public LogisticsIntegrationController(ContenedorService contenedorService,
            SolicitudService solicitudService,
            @Value("${integrations.logistics.shared-secret:logistics-shared-secret}") String sharedSecret) {
        this.contenedorService = contenedorService;
        this.solicitudService = solicitudService;
        this.sharedSecret = sharedSecret;
    }

    @PutMapping("/{solicitudId}/estado")
    public ResponseEntity<Void> actualizarEstadoDesdeLogistics(
            @PathVariable Long solicitudId,
            @Valid @RequestBody ContenedorEstadoUpdateRequest request,
            @RequestHeader(INTERNAL_SECRET_HEADER) String secret) {
        validarSecret(secret);
        contenedorService.actualizarEstadoDesdeLogistics(solicitudId, request, "logistics-system");
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{solicitudId}/costo")
    public ResponseEntity<Void> actualizarCostoDesdeLogistics(
            @PathVariable Long solicitudId,
            @Valid @RequestBody SolicitudCostoUpdateRequest request,
            @RequestHeader(INTERNAL_SECRET_HEADER) String secret) {
        validarSecret(secret);
        solicitudService.actualizarCosto(solicitudId, request);
        return ResponseEntity.accepted().build();
    }

    private void validarSecret(String providedSecret) {
        if (providedSecret == null || !providedSecret.equals(sharedSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Integración no autorizada");
        }
    }
}
