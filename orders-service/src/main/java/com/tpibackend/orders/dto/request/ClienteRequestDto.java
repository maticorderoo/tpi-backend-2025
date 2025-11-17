package com.tpibackend.orders.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteRequestDto {

    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombre;

    @NotBlank(message = "El email del cliente es obligatorio")
    @Email(message = "El email del cliente debe ser válido")
    private String email;

    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;

    @NotBlank(message = "El CUIT del cliente es obligatorio")
    @Size(max = 20, message = "El CUIT no puede superar los 20 caracteres")
    private String cuit;
}
