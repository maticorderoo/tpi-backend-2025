package com.tpibackend.logistics.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.logistics.dto.request.DepositoRequest;
import com.tpibackend.logistics.dto.response.DepositoResponse;
import com.tpibackend.logistics.service.DepositoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/logistics/depositos")
@Validated
@Tag(name = "Depósitos", description = "Gestión de depósitos intermedios")
@SecurityRequirement(name = "bearerAuth")
public class DepositoController {

    private final DepositoService depositoService;

    public DepositoController(DepositoService depositoService) {
        this.depositoService = depositoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar depósitos",
            description = "Obtiene todos los depósitos registrados. Requiere rol OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado recuperado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DepositoResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido")
            })
    public List<DepositoResponse> listar() {
        return depositoService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Obtener depósito por ID",
            description = "Recupera el detalle de un depósito. Requiere rol OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Depósito encontrado"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido"),
                    @ApiResponse(responseCode = "404", description = "Depósito no encontrado")
            })
    public DepositoResponse obtener(@PathVariable Long id) {
        return depositoService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear depósito",
            description = "Registra un nuevo depósito. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DepositoRequest.class),
                    examples = @ExampleObject(name = "crearDeposito",
                            value = "{\n  \"nombre\": \"Depósito Cuyo\",\n  \"direccion\": \"Av. Acceso Este 2400, Mendoza\",\n  \"lat\": -32.8908,\n  \"lng\": -68.8272,\n  \"costoEstadiaDia\": 15000\n}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Depósito creado"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido")
            })
    public DepositoResponse crear(@Valid @RequestBody DepositoRequest request) {
        return depositoService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar depósito",
            description = "Modifica los datos de un depósito existente. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DepositoRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Depósito actualizado"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido"),
                    @ApiResponse(responseCode = "404", description = "Depósito no encontrado")
            })
    public DepositoResponse actualizar(@PathVariable Long id, @Valid @RequestBody DepositoRequest request) {
        return depositoService.update(id, request);
    }
}
