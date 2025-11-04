package com.tpibackend.orders.client;

import java.math.BigDecimal;

public interface FleetMetricsClient {

    FleetAveragesResponse getFleetAverages();

    record FleetAveragesResponse(BigDecimal costoKilometroPromedio, BigDecimal consumoPromedio) {
    }
}
