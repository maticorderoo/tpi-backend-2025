package com.tpibackend.orders.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContenedorRequestDto {

    private Long id;

    private String codigo;

    @NotNull(message = "El peso del contenedor es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El peso del contenedor debe ser mayor a 0")
    private BigDecimal peso;

    @NotNull(message = "El volumen del contenedor es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El volumen del contenedor debe ser mayor a 0")
    private BigDecimal volumen;

    // El estado del contenedor se gestiona automáticamente según eventos del negocio
    // No debe ser proporcionado por el usuario
}
