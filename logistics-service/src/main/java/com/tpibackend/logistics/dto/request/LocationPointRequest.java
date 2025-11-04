package com.tpibackend.logistics.dto.request;

import com.tpibackend.logistics.model.enums.LocationType;

import jakarta.validation.constraints.NotNull;

public record LocationPointRequest(
        @NotNull LocationType tipo,
        Long referenciaId,
        Double lat,
        Double lng,
        String descripcion
) {
}
