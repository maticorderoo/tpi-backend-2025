package com.tpibackend.orders.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class EstimacionRequest {

    private String origen;

    private String destino;

    @NotNull(message = "El precio del combustible es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio del combustible debe ser mayor a 0")
    private BigDecimal precioCombustible;

    @NotNull(message = "La estadía estimada es obligatoria")
    @DecimalMin(value = "0.0", inclusive = true, message = "La estadía estimada no puede ser negativa")
    private BigDecimal estadiaEstimada;
}
