package com.tpibackend.fleet.model.dto;

import java.math.BigDecimal;

public record FleetMetricsResponse(
        BigDecimal consumoPromedio,
        BigDecimal costoKmPromedio
) {
}
