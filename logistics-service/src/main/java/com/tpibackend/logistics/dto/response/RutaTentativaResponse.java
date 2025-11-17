package com.tpibackend.logistics.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RutaTentativaResponse(
        Long solicitudId,
        Integer cantidadTramos,
        Integer cantidadDepositos,
        Double distanciaTotalKm,
        BigDecimal costoTotalAproximado,
        Long tiempoEstimadoMinutos,
        List<TramoTentativoResponse> tramos
) {
}
