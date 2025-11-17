package com.tpibackend.logistics.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RutaResponse(
        Long id,
        Long solicitudId,
        Integer cantTramos,
        Integer cantDepositos,
        BigDecimal costoTotalAprox,
        BigDecimal costoTotalReal,
        Long tiempoEstimadoMinutos,
        Long tiempoRealMinutos,
        BigDecimal pesoTotal,
        BigDecimal volumenTotal,
        List<TramoResponse> tramos
) {
}
