package com.tpibackend.logistics.dto.response;

import com.tpibackend.logistics.model.enums.TramoEstado;

public record PendingContainerResponse(
        Long solicitudId,
        Long rutaId,
        Long tramoId,
        TramoEstado estadoTramo,
        Long depositoDestinoId,
        String depositoDestinoNombre,
        String descripcionDestino
) {
}
