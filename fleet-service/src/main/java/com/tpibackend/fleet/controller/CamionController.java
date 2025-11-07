package com.tpibackend.fleet.controller;

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
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

@RestController
@RequestMapping("/camiones")
@Validated
@Tag(name = "Camiones")
@SecurityRequirement(name = "bearerAuth")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @GetMapping
    @Operation(summary = "Listado de camiones", description = "Obtiene el listado de camiones filtrando por disponibilidad",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado recuperado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class),
                                    examples = @ExampleObject(name = "camiones",
                                            value = "[{\n  \"id\": 12,\n  \"patente\": \"AA123BB\",\n  \"capacidadPeso\": 25000,\n  \"capacidadVolumen\": 60,\n  \"costoKm\": 950,\n  \"disponible\": true\n}]")))
            })
    public List<CamionResponse> listar(@RequestParam(value = "disponible", required = false) Boolean disponible) {
        return camionService.findAll(disponible);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de camión",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Camión encontrado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Camión no encontrado")
            })
    public CamionResponse obtener(@PathVariable Long id) {
        return camionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear camión",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CamionRequest.class),
                    examples = @ExampleObject(name = "crearCamion",
                            value = "{\n  \"patente\": \"AA123BB\",\n  \"capacidadPeso\": 25000,\n  \"capacidadVolumen\": 60,\n  \"costoKm\": 950,\n  \"consumoKm\": 0.32\n}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Camión creado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos"),
                    @ApiResponse(responseCode = "409", description = "Patente duplicada")
            })
    public CamionResponse crear(@Valid @RequestBody CamionRequest request) {
        return camionService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar camión",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CamionRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Camión actualizado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Camión no encontrado")
            })
    public CamionResponse actualizar(@PathVariable Long id, @Valid @RequestBody CamionRequest request) {
        return camionService.update(id, request);
    }

    @PutMapping("/{id}/disponibilidad")
    @Operation(summary = "Actualizar disponibilidad de camión",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CamionAvailabilityRequest.class),
                    examples = @ExampleObject(name = "actualizarDisponibilidad",
                            value = "{\n  \"disponible\": false,\n  \"motivoNoDisponibilidad\": \"Mantenimiento preventivo\"\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CamionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Camión no encontrado")
            })
    public CamionResponse actualizarDisponibilidad(@PathVariable Long id,
            @Valid @RequestBody CamionAvailabilityRequest request) {
        return camionService.updateAvailability(id, request);
    }
}
