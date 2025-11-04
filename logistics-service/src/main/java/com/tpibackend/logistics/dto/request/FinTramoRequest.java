package com.tpibackend.logistics.dto.request;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FinTramoRequest(
        OffsetDateTime fechaHoraFin,
        @Positive double kmReal,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal consumoLitrosKm,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal precioCombustible,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal costoKmBase,
        @NotNull @PositiveOrZero Integer diasEstadia
) {
}
