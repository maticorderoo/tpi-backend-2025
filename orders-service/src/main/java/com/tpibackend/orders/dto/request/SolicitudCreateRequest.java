package com.tpibackend.orders.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolicitudCreateRequest {

    @Valid
    @NotNull(message = "La información del cliente es obligatoria")
    private ClienteRequestDto cliente;

    @Valid
    @NotNull(message = "La información del contenedor es obligatoria")
    private ContenedorRequestDto contenedor;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    @NotNull(message = "La latitud de origen es obligatoria")
    private Double origenLat;

    @NotNull(message = "La longitud de origen es obligatoria")
    private Double origenLng;

    @NotBlank(message = "El destino es obligatorio")
    private String destino;

    @NotNull(message = "La latitud de destino es obligatoria")
    private Double destinoLat;

    @NotNull(message = "La longitud de destino es obligatoria")
    private Double destinoLng;
}
