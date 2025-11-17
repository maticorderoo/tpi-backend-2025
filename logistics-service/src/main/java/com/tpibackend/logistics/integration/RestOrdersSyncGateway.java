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
    public void notificarCosto(Long solicitudId, BigDecimal costoFinal) {
        log.debug("Notificando costo {} a Orders para solicitud {}", costoFinal, solicitudId);
        // TODO: reemplazar por publicación de eventos/logística cuando se disponga de broker.
        ordersClient.actualizarCosto(solicitudId, costoFinal);
    }
}
