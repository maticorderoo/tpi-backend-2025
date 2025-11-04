package com.tpibackend.fleet.controller;

import com.tpibackend.fleet.model.dto.CamionAvailabilityRequest;
import com.tpibackend.fleet.model.dto.CamionRequest;
import com.tpibackend.fleet.model.dto.CamionResponse;
import com.tpibackend.fleet.service.CamionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @GetMapping
    @Operation(summary = "Listado de camiones", description = "Obtiene el listado de camiones filtrando por disponibilidad")
    public List<CamionResponse> listar(@RequestParam(value = "disponible", required = false) Boolean disponible) {
        return camionService.findAll(disponible);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de camión")
    public CamionResponse obtener(@PathVariable Long id) {
        return camionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear camión")
    public CamionResponse crear(@Valid @RequestBody CamionRequest request) {
        return camionService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar camión")
    public CamionResponse actualizar(@PathVariable Long id, @Valid @RequestBody CamionRequest request) {
        return camionService.update(id, request);
    }

    @PutMapping("/{id}/disponibilidad")
    @Operation(summary = "Actualizar disponibilidad de camión")
    public CamionResponse actualizarDisponibilidad(@PathVariable Long id,
            @Valid @RequestBody CamionAvailabilityRequest request) {
        return camionService.updateAvailability(id, request);
    }
}
