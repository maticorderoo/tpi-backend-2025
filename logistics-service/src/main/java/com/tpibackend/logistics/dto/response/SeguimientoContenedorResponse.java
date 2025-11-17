package com.tpibackend.logistics.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record SeguimientoContenedorResponse(
        Long solicitudId,
        Long contenedorId,
        String contenedorCodigo,
        String estadoSolicitud,
        String estadoLogistico,
        SeguimientoUbicacion ubicacionActual,
        TramoResponse tramoActual,
        List<TramoResponse> tramos,
        OffsetDateTime fechaUltimoMovimiento) {
}
