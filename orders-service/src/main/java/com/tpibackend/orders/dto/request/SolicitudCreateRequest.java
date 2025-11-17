package com.tpibackend.orders.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitudCreateRequest {

    @Valid
    @NotNull(message = "La información del cliente es obligatoria")
    private ClienteRequestDto cliente;

    @Valid
    @NotNull(message = "La información del contenedor es obligatoria")
    private ContenedorRequestDto contenedor;

    @Valid
    @NotNull(message = "La ubicación de origen es obligatoria")
    private UbicacionRequestDto origen;

    @Valid
    @NotNull(message = "La ubicación de destino es obligatoria")
    private UbicacionRequestDto destino;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
    private String observaciones;
}
