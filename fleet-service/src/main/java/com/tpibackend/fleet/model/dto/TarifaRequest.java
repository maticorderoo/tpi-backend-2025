package com.tpibackend.fleet.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TarifaRequest(
        @NotBlank(message = "El tipo de tarifa es obligatorio")
        String tipo,

        @NotNull(message = "El valor es obligatorio")
        @Positive(message = "El valor debe ser positivo")
        BigDecimal valor
) {
}
