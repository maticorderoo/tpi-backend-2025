package com.tpibackend.orders.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SolicitudCostoUpdateRequest(
        @NotNull(message = "El costo final es obligatorio")
        BigDecimal costoFinal,
        Long tiempoRealMinutos
) {
}
