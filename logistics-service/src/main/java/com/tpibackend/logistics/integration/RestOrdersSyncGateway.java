package com.tpibackend.logistics.integration;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tpibackend.logistics.client.OrdersClient;

/**
 * Fachada temporal hacia el servicio de Orders mientras se implementan eventos/broker.
 */
@Component
public class RestOrdersSyncGateway implements OrdersSyncGateway {

    private static final Logger log = LoggerFactory.getLogger(RestOrdersSyncGateway.class);

    private final OrdersClient ordersClient;

    public RestOrdersSyncGateway(OrdersClient ordersClient) {
        this.ordersClient = ordersClient;
    }

    @Override
    public void notificarEstado(Long solicitudId, String estado) {
        log.debug("Notificando estado {} a Orders para solicitud {}", estado, solicitudId);
        // TODO: reemplazar por publicación de eventos/logística cuando se disponga de broker.
        ordersClient.actualizarEstado(solicitudId, estado);
    }

    @Override
    public void notificarCosto(Long solicitudId, BigDecimal costoFinal, Long tiempoRealMinutos) {
        log.debug("Notificando costo {} y tiempo real {} a Orders para solicitud {}", costoFinal, tiempoRealMinutos,
                solicitudId);
        // TODO: reemplazar por publicación de eventos/logística cuando se disponga de broker.
        ordersClient.actualizarCosto(solicitudId, costoFinal, tiempoRealMinutos);
    }

    @Override
    public void notificarPlanificacion(Long solicitudId, BigDecimal costoEstimado,
            Long tiempoEstimadoMinutos, Long rutaLogisticaId) {
        log.debug("Notificando planificación {} a Orders para solicitud {}", rutaLogisticaId, solicitudId);
        ordersClient.actualizarPlanificacion(solicitudId, costoEstimado, tiempoEstimadoMinutos, rutaLogisticaId);
    }
}
