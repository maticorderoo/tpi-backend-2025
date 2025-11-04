package com.tpibackend.logistics.client;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class FleetClient {

    private static final Logger log = LoggerFactory.getLogger(FleetClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FleetClient(RestTemplate restTemplate,
            @Value("${clients.fleet.base-url:http://fleet-service:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public TruckInfo obtenerCamion(Long camionId) {
        try {
            ResponseEntity<TruckInfo> response = restTemplate
                    .getForEntity(baseUrl + "/api/camiones/" + camionId, TruckInfo.class);
            if (response.getBody() == null) {
                throw new IllegalStateException("No se obtuvo información del camión " + camionId);
            }
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("No fue posible consultar FleetService, se utilizará información mínima: {}", ex.getMessage());
            return new TruckInfo(camionId, BigDecimal.valueOf(Double.MAX_VALUE),
                    BigDecimal.valueOf(Double.MAX_VALUE), true);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TruckInfo(Long id, BigDecimal capacidadPeso, BigDecimal capacidadVolumen, boolean disponible) {
    }
}
