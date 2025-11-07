package com.tpibackend.logistics.client;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OrdersClient {

    private static final Logger log = LoggerFactory.getLogger(OrdersClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public OrdersClient(RestTemplate restTemplate,
            @Value("${clients.orders.base-url:http://orders-service:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void actualizarEstado(Long solicitudId, String estado) {
        try {
            String estadoPayload = estado != null ? estado.toUpperCase(Locale.ROOT) : null;
            if (estadoPayload == null) {
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("estado", estadoPayload), headers);
            restTemplate.put(baseUrl + "/solicitudes/" + solicitudId + "/estado", entity);
        } catch (RestClientException ex) {
            log.warn("No se pudo notificar al servicio de Orders sobre el estado {}: {}", estado, ex.getMessage());
        }
    }

    public void actualizarCosto(Long solicitudId, BigDecimal costoFinal) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                    Map.of("costoFinal", costoFinal), headers);
            restTemplate.put(baseUrl + "/solicitudes/" + solicitudId + "/costo", entity);
        } catch (RestClientException ex) {
            log.warn("No se pudo actualizar el costo final de la solicitud {}: {}", solicitudId, ex.getMessage());
        }
    }
}
