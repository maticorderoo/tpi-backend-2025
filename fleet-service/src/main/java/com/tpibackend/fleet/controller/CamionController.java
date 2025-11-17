package com.tpibackend.fleet.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.fleet.model.dto.CamionAvailabilityRequest;
import com.tpibackend.fleet.model.dto.CamionRequest;
import com.tpibackend.fleet.model.dto.CamionResponse;
import com.tpibackend.fleet.service.CamionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/fleet/trucks")
@Validated
@Tag(name = "Camiones")
@SecurityRequirement(name = "bearerAuth")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    @Operation(summary = "Listado de camiones", description = "Obtiene el listado de camiones filtrando por disponibilidad. Requiere roles OPERADOR o TRANSPORTISTA.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado recuperado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class),
                                    examples = @ExampleObject(name = "camiones",
                                            value = "[{\n  \"id\": 12,\n  \"patente\": \"AA123BB\",\n  \"capacidadPeso\": 25000,\n  \"capacidadVolumen\": 60,\n  \"costoKm\": 950,\n  \"disponible\": true\n}]"))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}")))
            })
    public List<CamionResponse> listar(@RequestParam(value = "disponible", required = false) Boolean disponible) {
        return camionService.findAll(disponible);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    @Operation(summary = "Detalle de camión",
            description = "Recupera el detalle de un camión. Requiere roles OPERADOR o TRANSPORTISTA.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Camión encontrado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Camión no encontrado")
            })
    public CamionResponse obtener(@PathVariable Long id) {
        return camionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    @Operation(summary = "Crear camión",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CamionRequest.class),
                    examples = @ExampleObject(name = "crearCamion",
                            value = "{\n  \"dominio\": \"AA123BB\",\n  \"transportistaNombre\": \"Transportes del Sur S.A.\",\n  \"telefono\": \"+54-11-4567-8901\",\n  \"capPeso\": 25000,\n  \"capVolumen\": 60,\n  \"disponible\": true,\n  \"costoKmBase\": 950,\n  \"consumoLKm\": 0.32\n}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Camión creado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Patente duplicada")
            })
    public CamionResponse crear(@Valid @RequestBody CamionRequest request) {
        return camionService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    @Operation(summary = "Actualizar camión",
            description = "Actualiza los datos de un camión existente. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CamionRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Camión actualizado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Camión no encontrado")
            })
    public CamionResponse actualizar(@PathVariable Long id, @Valid @RequestBody CamionRequest request) {
        return camionService.update(id, request);
    }

    @PutMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    @Operation(summary = "Actualizar disponibilidad de camión",
            description = "Modifica el estado de disponibilidad de un camión. Requiere rol OPERADOR. Si otros servicios necesitan modificarlo, deben pasar por Gateway.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CamionAvailabilityRequest.class),
                    examples = @ExampleObject(name = "actualizarDisponibilidad",
                            value = "{\n  \"disponible\": false,\n  \"motivoNoDisponibilidad\": \"Mantenimiento preventivo\"\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Camión no encontrado")
            })
    public CamionResponse actualizarDisponibilidad(@PathVariable Long id,
            @Valid @RequestBody CamionAvailabilityRequest request) {
        return camionService.updateAvailability(id, request);
    }
}
