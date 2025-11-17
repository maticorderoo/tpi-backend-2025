package com.tpibackend.logistics.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CrearRutaRequest(
        @NotNull @Valid LocationPointRequest origen,
        @NotNull @Valid LocationPointRequest destino,
        @Valid List<DepositStopRequest> depositosIntermedios,
        BigDecimal pesoCarga,
        BigDecimal volumenCarga
) {
}
