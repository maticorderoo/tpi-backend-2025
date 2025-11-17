package com.tpibackend.orders.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class ContenedorRequestDto {

    private Long id;

    @Schema(description = "Código del contenedor", example = "CONT-XYZ-1234", required = true)
    @NotBlank(message = "El código del contenedor es obligatorio")
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
