package com.tpibackend.orders.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SolicitudEstadoUpdateRequest(
        @NotBlank(message = "El estado es obligatorio")
        String estado,
        String descripcion
) {
}
