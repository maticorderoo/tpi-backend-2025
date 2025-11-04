package com.tpibackend.fleet.model.dto;

import jakarta.validation.constraints.NotNull;

public record CamionAvailabilityRequest(
        @NotNull(message = "El estado de disponibilidad es obligatorio")
        Boolean disponible
) {
}
