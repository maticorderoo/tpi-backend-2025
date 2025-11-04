package com.tpibackend.logistics.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CrearRutaRequest(
        @NotNull @Valid LocationPointRequest origen,
        @NotNull @Valid LocationPointRequest destino,
        @Valid List<DepositStopRequest> depositosIntermedios,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal costoKmBase,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal consumoLitrosKm,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal precioCombustible,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal pesoCarga,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal volumenCarga
) {
}
