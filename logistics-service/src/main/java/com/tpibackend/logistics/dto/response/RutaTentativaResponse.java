package com.tpibackend.logistics.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.tpibackend.logistics.model.enums.RutaTentativaEstado;

public record RutaTentativaResponse(
        Long id,
        Long solicitudId,
        RutaTentativaEstado estado,
        Integer cantidadTramos,
        Integer cantidadDepositos,
        Double distanciaTotalKm,
        BigDecimal costoTotalAproximado,
        Long tiempoEstimadoMinutos,
        List<TramoTentativoResponse> tramos
) {
}
