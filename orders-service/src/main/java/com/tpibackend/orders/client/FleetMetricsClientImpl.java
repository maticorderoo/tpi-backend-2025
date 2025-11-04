package com.tpibackend.orders.client;

import com.tpibackend.orders.config.FleetClientProperties;
import com.tpibackend.orders.exception.OrdersIntegrationException;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class FleetMetricsClientImpl implements FleetMetricsClient {

    private static final Logger log = LoggerFactory.getLogger(FleetMetricsClientImpl.class);

    private final WebClient webClient;
    private final String metricsPath;

    public FleetMetricsClientImpl(WebClient.Builder builder, FleetClientProperties properties) {
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
        this.metricsPath = properties.metricsPath();
    }

    @Override
    public FleetAveragesResponse getFleetAverages() {
        try {
            FleetMetricsResponse response = webClient.get()
                .uri(metricsPath)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(FleetMetricsResponse.class)
                .switchIfEmpty(Mono.error(new OrdersIntegrationException("Respuesta vacía del servicio de flota")))
                .block();
            if (response == null) {
                throw new OrdersIntegrationException("No fue posible obtener los promedios de flota");
            }
            return new FleetAveragesResponse(response.costoKilometroPromedio, response.consumoPromedio);
        } catch (OrdersIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error consultando servicio de flota", ex);
            throw new OrdersIntegrationException("Error consultando servicio de flota", ex);
        }
    }

    private static class FleetMetricsResponse {
        private BigDecimal costoKilometroPromedio;
        private BigDecimal consumoPromedio;

        public BigDecimal getCostoKilometroPromedio() {
            return costoKilometroPromedio;
        }

        public void setCostoKilometroPromedio(BigDecimal costoKilometroPromedio) {
            this.costoKilometroPromedio = costoKilometroPromedio;
        }

        public BigDecimal getConsumoPromedio() {
            return consumoPromedio;
        }

        public void setConsumoPromedio(BigDecimal consumoPromedio) {
            this.consumoPromedio = consumoPromedio;
        }
    }
}
