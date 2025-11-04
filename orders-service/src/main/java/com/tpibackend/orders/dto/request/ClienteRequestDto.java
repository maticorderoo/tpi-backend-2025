package com.tpibackend.orders.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteRequestDto {

    private Long id;

    private String nombre;

    @Email(message = "El email del cliente debe ser válido")
    private String email;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;
}
