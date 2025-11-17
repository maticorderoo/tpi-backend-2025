package com.tpibackend.orders.controller;

import com.tpibackend.orders.dto.response.PendingContainerResponseDto;
import com.tpibackend.orders.service.ContenedorQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders/containers")
@Validated
@Tag(name = "Contenedores", description = "Consultas de contenedores desde Orders")
@SecurityRequirement(name = "bearerAuth")
public class ContenedorQueryController {

    private final ContenedorQueryService contenedorQueryService;

    public ContenedorQueryController(ContenedorQueryService contenedorQueryService) {
        this.contenedorQueryService = contenedorQueryService;
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar contenedores pendientes",
            description = "Devuelve los contenedores con tramos logísticos pendientes filtrando opcionalmente por estado o depósito.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PendingContainerResponseDto.class)))),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido")
            })
    public ResponseEntity<List<PendingContainerResponseDto>> obtenerPendientes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long depositoId) {
        return ResponseEntity.ok(contenedorQueryService.obtenerContenedoresPendientes(estado, depositoId));
    }
}
