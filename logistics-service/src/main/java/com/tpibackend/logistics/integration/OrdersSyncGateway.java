package com.tpibackend.logistics.integration;

import java.math.BigDecimal;

public interface OrdersSyncGateway {

    void notificarEstado(Long solicitudId, String estado);

    void notificarCosto(Long solicitudId, BigDecimal costoFinal);

    void notificarPlanificacion(Long solicitudId, BigDecimal costoEstimado,
            Long tiempoEstimadoMinutos, Long rutaLogisticaId);
}
