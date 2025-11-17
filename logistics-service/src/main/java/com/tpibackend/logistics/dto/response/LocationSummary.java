package com.tpibackend.logistics.dto.response;

import com.tpibackend.logistics.model.enums.LocationType;

public record LocationSummary(
        LocationType tipo,
        Long referenciaId,
        String descripcion,
        Double latitud,
        Double longitud
) {
}
