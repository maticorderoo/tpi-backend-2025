package com.tpibackend.orders.controller;

import java.math.BigDecimal;

import com.tpibackend.orders.dto.request.ContenedorEstadoUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudCostoUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudPlanificacionUpdateRequest;
import com.tpibackend.orders.dto.response.SolicitudLogisticsView;
import com.tpibackend.orders.dto.response.SolicitudLogisticsView.Punto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.service.ContenedorService;
import com.tpibackend.orders.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PutMapping("/{solicitudId}/planificacion")
    public ResponseEntity<Void> actualizarPlanificacionDesdeLogistics(
            @PathVariable Long solicitudId,
            @Valid @RequestBody SolicitudPlanificacionUpdateRequest request,
            @RequestHeader(INTERNAL_SECRET_HEADER) String secret) {
        validarSecret(secret);
        solicitudService.actualizarPlanificacion(solicitudId, request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{solicitudId}")
    public ResponseEntity<SolicitudLogisticsView> obtenerSolicitudParaLogistics(
            @PathVariable Long solicitudId,
            @RequestHeader(INTERNAL_SECRET_HEADER) String secret) {
        validarSecret(secret);
        SolicitudResponseDto dto = solicitudService.obtenerSolicitud(solicitudId);
        return ResponseEntity.ok(mapSolicitud(dto));
    }

    private void validarSecret(String providedSecret) {
        if (providedSecret == null || !providedSecret.equals(sharedSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Integración no autorizada");
        }
    }

    private SolicitudLogisticsView mapSolicitud(SolicitudResponseDto dto) {
        Punto origen = new Punto(dto.getOrigen(), dto.getOrigenLat(), dto.getOrigenLng());
        Punto destino = new Punto(dto.getDestino(), dto.getDestinoLat(), dto.getDestinoLng());
        BigDecimal peso = dto.getContenedor() != null ? dto.getContenedor().getPeso() : null;
        BigDecimal volumen = dto.getContenedor() != null ? dto.getContenedor().getVolumen() : null;
        return new SolicitudLogisticsView(dto.getId(), origen, destino, peso, volumen);
    }
}
