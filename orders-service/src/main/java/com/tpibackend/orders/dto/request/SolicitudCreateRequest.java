package com.tpibackend.orders.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SolicitudCreateRequest {

    @Valid
    @NotNull(message = "La información del cliente es obligatoria")
    private ClienteRequestDto cliente;

    @Valid
    @NotNull(message = "La información del contenedor es obligatoria")
    private ContenedorRequestDto contenedor;

    private String origen;

    private String destino;

    private BigDecimal estadiaEstimada;
}
