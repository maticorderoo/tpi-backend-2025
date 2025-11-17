package com.tpibackend.orders.dto.request;

import com.tpibackend.orders.model.enums.ContenedorEstado;
import jakarta.validation.constraints.NotNull;

/**
 * Payload interno utilizado por Logistics para propagar estados del contenedor.
 */
public record ContenedorEstadoUpdateRequest(
    @NotNull(message = "El estado es obligatorio")
    ContenedorEstado estado,
    
    String observaciones
) {}
