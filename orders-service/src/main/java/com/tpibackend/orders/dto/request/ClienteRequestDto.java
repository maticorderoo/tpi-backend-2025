package com.tpibackend.orders.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteRequestDto {

    private Long id;
    // El resto de campos pueden ser opcionales en el request cuando se envía solo el CUIT
    // (se validará en el service si es necesario crear un nuevo cliente)
    private String nombre;

    @Email(message = "El email del cliente debe ser válido")
    private String email;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;

    @NotBlank(message = "El CUIT del cliente es obligatorio")
    @Size(max = 20, message = "El CUIT no puede superar los 20 caracteres")
    private String cuit;
}
