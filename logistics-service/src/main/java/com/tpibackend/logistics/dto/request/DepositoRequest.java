package com.tpibackend.logistics.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DepositoRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    String direccion,

    @NotNull(message = "La latitud es obligatoria")
    Double lat,

    @NotNull(message = "La longitud es obligatoria")
    Double lng,

    @NotNull(message = "El costo de estadía diario es obligatorio")
    @Positive(message = "El costo de estadía debe ser positivo")
    BigDecimal costoEstadiaDia
) {
}
