package com.tpibackend.fleet.model.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TarifaResponse(
        Long id,
        String nombre,
        BigDecimal costoKm,
        BigDecimal costoHora,
        String moneda,
        OffsetDateTime creadaEn
) {
}
