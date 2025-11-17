package com.tpibackend.orders.dto.request;

import com.tpibackend.orders.model.enums.ContenedorEstado;
import jakarta.validation.constraints.NotNull;

/**
 * Payload interno utilizado por Logistics para propagar estados del contenedor.
 */
public record ContenedorEstadoUpdateRequest(
    @NotNull(message = "El estado de la solicitud es obligatorio")
    ContenedorEstado estadoSolicitud,

    @NotNull(message = "El estado del contenedor es obligatorio")
    ContenedorEstado estadoContenedor,

    String observaciones
) {}
