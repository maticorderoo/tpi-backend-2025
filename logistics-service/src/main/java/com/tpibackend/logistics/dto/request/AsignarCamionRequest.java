package com.tpibackend.logistics.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record AsignarCamionRequest(
        @NotNull Long camionId,
        BigDecimal pesoCarga,
        BigDecimal volumenCarga
) {
}
