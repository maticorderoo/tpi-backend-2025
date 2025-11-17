package com.tpibackend.fleet.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TarifaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotNull(message = "El costo por kilómetro es obligatorio")
        @Positive(message = "El costo por kilómetro debe ser positivo")
        BigDecimal costoKm,

        @NotNull(message = "El costo por hora es obligatorio")
        @Positive(message = "El costo por hora debe ser positivo")
        BigDecimal costoHora,

        @NotBlank(message = "La moneda es obligatoria")
        String moneda
) {
}
