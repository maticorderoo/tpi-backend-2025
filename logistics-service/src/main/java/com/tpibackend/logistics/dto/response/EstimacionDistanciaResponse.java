package com.tpibackend.logistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response básico para exponer distancia y duración.
 */
public record EstimacionDistanciaResponse(
        @Schema(description = "Distancia calculada en kilómetros", example = "320.5")
        double distanciaKm,
        @Schema(description = "Duración estimada en minutos", example = "285.4")
        double duracionMinutos
) {
}
