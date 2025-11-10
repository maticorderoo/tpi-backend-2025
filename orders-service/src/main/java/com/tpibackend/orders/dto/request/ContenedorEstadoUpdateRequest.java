package com.tpibackend.orders.dto.request;

import com.tpibackend.orders.model.enums.ContenedorEstado;
import jakarta.validation.constraints.NotNull;

/**
 * Request para actualizar manualmente el estado de un contenedor.
 * Solo disponible para usuarios con rol OPERADOR.
 */
public record ContenedorEstadoUpdateRequest(
    @NotNull(message = "El estado es obligatorio")
    ContenedorEstado estado,
    
    String observaciones
) {}
