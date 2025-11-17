package com.tpibackend.logistics.dto.request;

import jakarta.validation.constraints.NotNull;

public record AsignarCamionRequest(@NotNull Long camionId) {
}
