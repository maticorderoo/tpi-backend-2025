package com.tpibackend.logistics.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DepositStopRequest(
        @NotNull Long depositoId,
        @Min(0) int diasEstadia
) {
}
