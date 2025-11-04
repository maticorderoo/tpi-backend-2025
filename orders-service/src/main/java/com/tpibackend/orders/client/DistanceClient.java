package com.tpibackend.orders.client;

import java.math.BigDecimal;

public interface DistanceClient {

    DistanceResponse calculateDistance(String origin, String destination);

    record DistanceResponse(BigDecimal distanciaKilometros, long duracionMinutos) {
    }
}
