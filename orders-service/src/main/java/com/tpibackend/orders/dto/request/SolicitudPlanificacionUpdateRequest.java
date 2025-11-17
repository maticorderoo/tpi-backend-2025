package com.tpibackend.orders.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record SolicitudPlanificacionUpdateRequest(
        @NotNull(message = "El costo estimado es obligatorio")
        BigDecimal costoEstimado,
        @NotNull(message = "El tiempo estimado es obligatorio")
        Long tiempoEstimadoMinutos,
        @NotNull(message = "La ruta logística es obligatoria")
        Long rutaLogisticaId
) {
}
