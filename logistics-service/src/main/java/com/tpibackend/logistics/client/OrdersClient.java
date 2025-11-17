package com.tpibackend.logistics.client;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.tpibackend.logistics.dto.integration.SolicitudLogisticaResponse;

@Component
public class OrdersClient {

    private static final Logger log = LoggerFactory.getLogger(OrdersClient.class);
    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String sharedSecret;

    public OrdersClient(RestTemplate restTemplate,
            @Value("${clients.orders.base-url:http://orders-service:8080/api/orders/internal/logistics}") String baseUrl,
            @Value("${clients.orders.shared-secret:logistics-shared-secret}") String sharedSecret) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.sharedSecret = sharedSecret;
    }

    public void actualizarEstado(Long solicitudId, String estadoSolicitud, String estadoContenedor) {
        try {
            Map<String, Object> payload = new HashMap<>();
            if (estadoSolicitud != null) {
                payload.put("estadoSolicitud", estadoSolicitud.toUpperCase(Locale.ROOT));
            }
            if (estadoContenedor != null) {
                payload.put("estadoContenedor", estadoContenedor.toUpperCase(Locale.ROOT));
            }
            if (payload.isEmpty()) {
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(INTERNAL_SECRET_HEADER, sharedSecret);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.put(baseUrl + "/" + solicitudId + "/estado", entity);
        } catch (RestClientException ex) {
            log.warn("No se pudo notificar al servicio de Orders sobre el estado {}-{}: {}",
                    estadoSolicitud, estadoContenedor, ex.getMessage());
        }
    }

    public void actualizarCosto(Long solicitudId, BigDecimal costoFinal, Long tiempoRealMinutos) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(INTERNAL_SECRET_HEADER, sharedSecret);
            Map<String, Object> payload = new HashMap<>();
            payload.put("costoFinal", costoFinal);
            if (tiempoRealMinutos != null) {
                payload.put("tiempoRealMinutos", tiempoRealMinutos);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.put(baseUrl + "/" + solicitudId + "/costo", entity);
        } catch (RestClientException ex) {
            log.warn("No se pudo actualizar el costo final de la solicitud {}: {}", solicitudId, ex.getMessage());
        }
    }

    public void actualizarPlanificacion(Long solicitudId, BigDecimal costoEstimado,
            Long tiempoEstimadoMinutos, Long rutaLogisticaId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(INTERNAL_SECRET_HEADER, sharedSecret);
            PlanificacionPayload payload = new PlanificacionPayload(costoEstimado, tiempoEstimadoMinutos, rutaLogisticaId);
            HttpEntity<PlanificacionPayload> entity = new HttpEntity<>(payload, headers);
            restTemplate.put(baseUrl + "/" + solicitudId + "/planificacion", entity);
        } catch (RestClientException ex) {
            log.warn("No se pudo actualizar la planificación de la solicitud {}: {}", solicitudId, ex.getMessage());
        }
    }

    public void actualizarFinalizacion(Long solicitudId, String estadoSolicitud, String estadoContenedor,
            BigDecimal costoFinal, Long tiempoRealMinutos) {
        actualizarEstado(solicitudId, estadoSolicitud, estadoContenedor);
        actualizarCosto(solicitudId, costoFinal, tiempoRealMinutos);
    }

    public Optional<SolicitudLogisticaResponse> obtenerSolicitud(Long solicitudId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(INTERNAL_SECRET_HEADER, sharedSecret);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<SolicitudLogisticaResponse> response = restTemplate.exchange(
                    baseUrl + "/" + solicitudId,
                    HttpMethod.GET,
                    entity,
                    SolicitudLogisticaResponse.class);
            return Optional.ofNullable(response.getBody());
        } catch (RestClientException ex) {
            log.warn("No se pudo obtener la solicitud {} desde Orders: {}", solicitudId, ex.getMessage());
            return Optional.empty();
        }
    }

    private record PlanificacionPayload(BigDecimal costoEstimado, Long tiempoEstimadoMinutos,
            Long rutaLogisticaId) {
    }
}
