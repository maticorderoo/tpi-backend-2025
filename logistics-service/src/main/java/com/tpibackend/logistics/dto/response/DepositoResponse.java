package com.tpibackend.logistics.dto.response;

import java.math.BigDecimal;

public record DepositoResponse(
        Long id,
        String nombre,
        String direccion,
        Double lat,
        Double lng,
        BigDecimal costoEstadiaDia
) {
}
