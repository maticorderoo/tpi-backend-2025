package com.tpibackend.logistics.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.model.enums.TramoTipo;

public record TramoResponse(
        Long id,
        Long rutaId,
        LocationType origenTipo,
        Long origenId,
        LocationType destinoTipo,
        Long destinoId,
        TramoTipo tipo,
        TramoEstado estado,
        BigDecimal costoAprox,
        BigDecimal costoReal,
        OffsetDateTime fechaHoraInicio,
        OffsetDateTime fechaHoraFin,
        Long camionId,
        Double distanciaKmEstimada,
        Double distanciaKmReal,
        Long tiempoEstimadoMinutos,
        Long tiempoRealMinutos,
        Integer diasEstadia,
        BigDecimal costoEstadiaDia,
        BigDecimal costoEstadia
) {
}
