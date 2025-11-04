package com.tpibackend.fleet.model.dto;

import java.math.BigDecimal;

public record TarifaResponse(
        Long id,
        String tipo,
        BigDecimal valor
) {
}
