package com.tpibackend.fleet.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.tpibackend.fleet.model.dto.TarifaRequest;
import com.tpibackend.fleet.model.dto.TarifaResponse;
import com.tpibackend.fleet.service.TarifaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/fleet/tarifas")
@Validated
@Tag(name = "Tarifas", description = "Gestión de tarifas de transporte")
@SecurityRequirement(name = "bearerAuth")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    @Operation(summary = "Listado de tarifas", description = "Obtiene todas las tarifas configuradas. Requiere rol OPERADOR.")
    public List<TarifaResponse> listar() {
        return tarifaService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    @Operation(summary = "Crear tarifa", description = "Registra una nueva tarifa. Requiere rol OPERADOR.")
    public TarifaResponse crear(@Valid @RequestBody TarifaRequest request) {
        return tarifaService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
    @Operation(summary = "Actualizar tarifa", description = "Modifica los valores de una tarifa existente. Requiere rol OPERADOR.")
    public TarifaResponse actualizar(@PathVariable Long id, @Valid @RequestBody TarifaRequest request) {
        return tarifaService.update(id, request);
    }
}
