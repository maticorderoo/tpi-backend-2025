package com.tpibackend.orders.client;

import com.tpibackend.orders.config.DistanceClientProperties;
import com.tpibackend.orders.exception.OrdersIntegrationException;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class DistanceClientImpl implements DistanceClient {

    private static final Logger log = LoggerFactory.getLogger(DistanceClientImpl.class);

    private final WebClient webClient;
    private final String routePath;

    public DistanceClientImpl(WebClient.Builder builder, DistanceClientProperties properties) {
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
        this.routePath = properties.routePath();
    }

    @Override
    public DistanceResponse calculateDistance(String origin, String destination) {
        try {
            DistanceRequest request = new DistanceRequest(origin, destination);
            DistanceApiResponse response = webClient.post()
                .uri(routePath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DistanceApiResponse.class)
                .switchIfEmpty(Mono.error(new OrdersIntegrationException("Respuesta vacía del servicio de distancias")))
                .block();
            if (response == null) {
                throw new OrdersIntegrationException("No fue posible obtener la distancia estimada");
            }
            return new DistanceResponse(response.distanceKilometers, response.durationMinutes);
        } catch (OrdersIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error consultando servicio de distancias", ex);
            throw new OrdersIntegrationException("Error consultando servicio de distancias", ex);
        }
    }

    private record DistanceRequest(String origin, String destination) {
    }

    private static class DistanceApiResponse {
        private BigDecimal distanceKilometers;
        private long durationMinutes;

        public BigDecimal getDistanceKilometers() {
            return distanceKilometers;
        }

        public void setDistanceKilometers(BigDecimal distanceKilometers) {
            this.distanceKilometers = distanceKilometers;
        }

        public long getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(long durationMinutes) {
            this.durationMinutes = durationMinutes;
        }
    }
}
