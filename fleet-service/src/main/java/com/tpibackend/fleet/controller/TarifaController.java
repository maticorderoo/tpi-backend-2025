package com.tpibackend.fleet.controller;

import com.tpibackend.fleet.model.dto.TarifaRequest;
import com.tpibackend.fleet.model.dto.TarifaResponse;
import com.tpibackend.fleet.service.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tarifas")
@Validated
@Tag(name = "Tarifas")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    @Operation(summary = "Listado de tarifas")
    public List<TarifaResponse> listar() {
        return tarifaService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear tarifa")
    public TarifaResponse crear(@Valid @RequestBody TarifaRequest request) {
        return tarifaService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarifa")
    public TarifaResponse actualizar(@PathVariable Long id, @Valid @RequestBody TarifaRequest request) {
        return tarifaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar tarifa")
    public void eliminar(@PathVariable Long id) {
        tarifaService.delete(id);
    }
}
