package com.tpibackend.fleet.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CamionRequest(
        @NotBlank(message = "El dominio es obligatorio")
        @Pattern(regexp = "^[A-Z]{2,3}[0-9]{3}([A-Z]{2})?$", message = "Formato de dominio inválido")
        String dominio,

        @NotBlank(message = "El nombre del transportista es obligatorio")
        String transportistaNombre,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,

        @NotNull(message = "La capacidad de peso es obligatoria")
        @Positive(message = "La capacidad de peso debe ser positiva")
        BigDecimal capPeso,

        @NotNull(message = "La capacidad de volumen es obligatoria")
        @Positive(message = "La capacidad de volumen debe ser positiva")
        BigDecimal capVolumen,

        @NotNull(message = "La disponibilidad es obligatoria")
        Boolean disponible,

        @NotNull(message = "El costo base por km es obligatorio")
        @Positive(message = "El costo base por km debe ser positivo")
        BigDecimal costoKmBase,

        @NotNull(message = "El consumo por km es obligatorio")
        @Positive(message = "El consumo por km debe ser positivo")
        BigDecimal consumoLKm
) {
}
