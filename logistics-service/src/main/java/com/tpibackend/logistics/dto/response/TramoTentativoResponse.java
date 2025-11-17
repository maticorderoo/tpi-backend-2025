package com.tpibackend.logistics.dto.response;

import java.math.BigDecimal;

import com.tpibackend.logistics.model.enums.TramoTipo;

public record TramoTentativoResponse(
        LocationSummary origen,
        LocationSummary destino,
        TramoTipo tipo,
        Double distanciaKm,
        Long tiempoEstimadoMinutos,
        BigDecimal costoAproximado
) {
}
