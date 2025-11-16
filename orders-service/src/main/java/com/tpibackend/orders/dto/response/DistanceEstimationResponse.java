package com.tpibackend.orders.dto.response;

import java.math.BigDecimal;

/**
 * DTO local para desacoplar Orders de distance-client.
 */
public record DistanceEstimationResponse(double distanciaKm, double duracionMinutos) {
    public BigDecimal distanciaComoBigDecimal() {
        return BigDecimal.valueOf(distanciaKm);
    }
}
