package com.tpibackend.logistics.dto.response;

import com.tpibackend.logistics.model.enums.LocationType;

public record SeguimientoUbicacion(
        LocationType tipo,
        Long referenciaId,
        Double latitud,
        Double longitud,
        String descripcion) {
}
