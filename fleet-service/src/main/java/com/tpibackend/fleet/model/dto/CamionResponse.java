package com.tpibackend.fleet.model.dto;

import java.math.BigDecimal;

public record CamionResponse(
        Long id,
        String dominio,
        String transportistaNombre,
        String telefono,
        BigDecimal capPeso,
        BigDecimal capVolumen,
        Boolean disponible,
        BigDecimal costoKmBase,
        BigDecimal consumoLKm
) {
}
