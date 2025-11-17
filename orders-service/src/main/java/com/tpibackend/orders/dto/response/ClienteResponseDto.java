package com.tpibackend.orders.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClienteResponseDto {
    Long id;
    String nombre;
    String email;
    String telefono;
    String cuit;
}
