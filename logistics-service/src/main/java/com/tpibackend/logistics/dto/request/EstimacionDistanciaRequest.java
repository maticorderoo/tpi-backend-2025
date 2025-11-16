package com.tpibackend.logistics.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request simple para exponer el cálculo de distancia como capability propia de Logistics.
 */
public record EstimacionDistanciaRequest(
        @Schema(description = "Dirección u origen libre aceptado por Google Maps", example = "Buenos Aires")
        @NotBlank(message = "El origen es obligatorio")
        String origen,
        @Schema(description = "Dirección o coordenadas destino", example = "Rosario")
        @NotBlank(message = "El destino es obligatorio")
        String destino
) {
}
