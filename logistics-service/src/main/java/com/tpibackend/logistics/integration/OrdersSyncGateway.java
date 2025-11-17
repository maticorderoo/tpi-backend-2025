package com.tpibackend.logistics.integration;

import java.math.BigDecimal;

public interface OrdersSyncGateway {

    void notificarEstado(Long solicitudId, String estadoSolicitud, String estadoContenedor);

    void notificarCosto(Long solicitudId, BigDecimal costoFinal, Long tiempoRealMinutos);

    void notificarFinalizacion(Long solicitudId, String estadoSolicitud, String estadoContenedor,
            BigDecimal costoFinal, Long tiempoRealMinutos);

    void notificarPlanificacion(Long solicitudId, BigDecimal costoEstimado,
            Long tiempoEstimadoMinutos, Long rutaLogisticaId);
}
