package com.tpibackend.logistics.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.logistics.dto.response.PendingContainerResponse;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.service.ContenedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/logistics/containers")
@Validated
@Tag(name = "Contenedores", description = "Consulta de contenedores pendientes")
@SecurityRequirement(name = "bearerAuth")
public class ContenedorController {

    private final ContenedorService contenedorService;

    public ContenedorController(ContenedorService contenedorService) {
        this.contenedorService = contenedorService;
    }

    // TODO: mover este controlador y el servicio asociado a Orders; Logistics no debe ser dueño de contenedores

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Contenedores pendientes",
            description = "Lista los contenedores que tienen tramos pendientes filtrando opcionalmente por estado y depósito. Requiere rol OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PendingContainerResponse.class),
                                    examples = @ExampleObject(name = "contenedoresPendientes",
                                            value = "[{\n  \"solicitudId\": 42,\n  \"rutaId\": 21,\n  \"tramoId\": 55,\n  \"estadoTramo\": \"ASIGNADO\",\n  \"depositoDestinoId\": 3,\n  \"depositoDestinoNombre\": \"Depósito Cuyo\"\n}]"))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}")))
            })
    public ResponseEntity<List<PendingContainerResponse>> obtenerPendientes(
            @RequestParam(required = false) TramoEstado estado,
            @RequestParam(required = false) Long depositoId) {
        return ResponseEntity.ok(contenedorService.obtenerContenedoresPendientes(estado, depositoId));
    }
}
