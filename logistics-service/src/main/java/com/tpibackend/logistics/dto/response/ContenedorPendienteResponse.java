package com.tpibackend.logistics.dto.response;

import java.time.OffsetDateTime;

import com.tpibackend.logistics.model.enums.TramoEstado;

public record ContenedorPendienteResponse(
        Long solicitudId,
        Long contenedorId,
        String contenedorCodigo,
        String estadoSolicitud,
        Long rutaId,
        Long tramoActualId,
        String estadoLogistico,
        TramoEstado estadoTramoActual,
        SeguimientoUbicacion ubicacionActual,
        OffsetDateTime fechaUltimoMovimiento) {
}
