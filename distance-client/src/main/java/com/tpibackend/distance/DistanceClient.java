package com.tpibackend.distance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.tpibackend.distance.mapper.DistanceResponseMapper;
import com.tpibackend.distance.model.DirectionsApiResponse;
import com.tpibackend.distance.model.DistanceData;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Cliente para calcular distancia y tiempo entre coordenadas usando Google Directions API.
 * Soporta múltiples tramos (legs), manejo de errores, timeouts y logging.
 */
@Slf4j
@Component
public class DistanceClient {

    private final WebClient googleWebClient;

    @Value("${google.maps.api.key}")
    private String apiKey;

    public DistanceClient(WebClient googleWebClient) {
        this.googleWebClient = googleWebClient;
    }

    /**
     * Calcula distancia y duración entre dos coordenadas con modo "driving" por defecto.
     */
    public DistanceData getDistance(double fromLat, double fromLng,
                                    double toLat, double toLng) {
        return getDistance(fromLat, fromLng, toLat, toLng, "driving");
    }

    /**
     * Calcula distancia y duración entre dos coordenadas con modo de viaje especificado.
     * 
     * @param fromLat latitud origen
     * @param fromLng longitud origen
     * @param toLat latitud destino
     * @param toLng longitud destino
     * @param mode modo de viaje: "driving", "walking", "bicycling", "transit"
     * @return DistanceData con distancia en km y duración en minutos
     * @throws IllegalStateException si hay errores en la respuesta o key inválida
     */
    public DistanceData getDistance(double fromLat, double fromLng,
                                    double toLat, double toLng,
                                    String mode) {
        String origin = fromLat + "," + fromLng;
        String destination = toLat + "," + toLng;
        
        log.info("Solicitando ruta: origin={}, destination={}, mode={}", origin, destination, mode);

        try {
            DirectionsApiResponse response = googleWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("origin", origin)
                            .queryParam("destination", destination)
                            .queryParam("mode", mode)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> 
                        clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Error 4xx en Google Directions API: {}", body);
                                if (body.contains("REQUEST_DENIED") || body.contains("INVALID_REQUEST")) {
                                    return Mono.error(new IllegalStateException(
                                        "API Key inválida o solicitud incorrecta. Verifica tu configuración."));
                                }
                                return Mono.error(new IllegalStateException(
                                    "Error del cliente (4xx): " + body));
                            })
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, serverResponse -> 
                        serverResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Error 5xx en Google Directions API: {}", body);
                                return Mono.error(new IllegalStateException(
                                    "Error del servidor de Google Maps (5xx): " + body));
                            })
                    )
                    .bodyToMono(DirectionsApiResponse.class)
                    .block();

            if (response == null) {
                log.error("Respuesta vacía de Google Directions API");
                throw new IllegalStateException("La respuesta de Google Directions API está vacía");
            }

            // Usar el mapper para convertir la respuesta
            DistanceData result = DistanceResponseMapper.from(response);
            
            log.info("Ruta calculada exitosamente: {:.2f} km, {:.2f} min", 
                result.distanceKm(), result.durationMinutes());
            
            return result;

        } catch (WebClientResponseException e) {
            log.error("Error HTTP al llamar a Google Directions API: status={}, body={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException(
                "Error al conectar con Google Directions API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al calcular distancia", e);
            throw new IllegalStateException(
                "Error al calcular distancia: " + e.getMessage(), e);
        }
    }
}
