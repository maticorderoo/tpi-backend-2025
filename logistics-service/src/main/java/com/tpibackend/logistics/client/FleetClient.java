package com.tpibackend.logistics.client;

import java.math.BigDecimal;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tpibackend.logistics.exception.BusinessException;
import com.tpibackend.logistics.exception.NotFoundException;

@Component
public class FleetClient {

    private static final Logger log = LoggerFactory.getLogger(FleetClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FleetClient(RestTemplate restTemplate,
            @Value("${clients.fleet.base-url:http://fleet-service:8080/api}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public TruckLookupResult obtenerCamion(Long camionId) {
        URI uri = buildUri("/fleet/trucks/{id}", camionId);
        log.info("Consultando FleetService GET {}", uri);
        try {
            ResponseEntity<TruckInfo> response = restTemplate.getForEntity(uri, TruckInfo.class);
            log.info("FleetService GET {} -> {}", uri, response.getStatusCode().value());
            if (response.getBody() == null) {
                throw new BusinessException("No se obtuvo información del camión " + camionId);
            }
            return new TruckLookupResult(response.getBody(), uri, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            log.error("FleetService GET {} respondió {} {}", uri, ex.getStatusCode().value(), ex.getStatusText());
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new NotFoundException("Camión " + camionId + " no encontrado en Flota");
            }
            throw new BusinessException("No se pudo obtener el camión desde Flota: " + ex.getStatusCode());
        } catch (RestClientException ex) {
            log.error("Error consultando FleetService GET {}: {}", uri, ex.getMessage());
            throw new BusinessException("Error consultando FleetService: " + ex.getMessage());
        }
    }

    public void actualizarDisponibilidad(Long camionId, boolean disponible, String motivo) {
        URI uri = buildUri("/fleet/trucks/{id}/disponibilidad", camionId);
        log.info("FleetService PUT {} disponible={} motivo={}", uri, disponible, motivo);
        try {
            DisponibilidadRequest request = new DisponibilidadRequest(disponible, motivo);
            restTemplate.put(uri, request);
            log.info("Disponibilidad del camión {} actualizada a: {}", camionId, disponible);
        } catch (HttpStatusCodeException ex) {
            log.error("FleetService PUT {} respondió {} {}", uri, ex.getStatusCode().value(), ex.getStatusText());
        } catch (RestClientException ex) {
            log.error("Error actualizando disponibilidad del camión {}: {}", camionId, ex.getMessage());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TruckInfo(Long id,
            @JsonProperty("capPeso") BigDecimal capacidadPeso,
            @JsonProperty("capVolumen") BigDecimal capacidadVolumen,
            @JsonProperty("disponible") Boolean disponible) {
    }

    public record DisponibilidadRequest(Boolean disponible, String motivoNoDisponibilidad) {
    }

    public record TruckLookupResult(TruckInfo truck, URI uri, HttpStatusCode statusCode) {
    }

    private URI buildUri(String path, Object... uriVariables) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(path)
                .buildAndExpand(uriVariables)
                .toUri();
    }
}
