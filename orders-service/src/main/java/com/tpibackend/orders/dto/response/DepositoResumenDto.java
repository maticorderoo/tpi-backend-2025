package com.tpibackend.orders.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DepositoResumenDto {
    Long id;
    String nombre;
    String direccion;
}
