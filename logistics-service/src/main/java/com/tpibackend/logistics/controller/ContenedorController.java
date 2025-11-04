package com.tpibackend.logistics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpibackend.logistics.dto.response.PendingContainerResponse;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.service.ContenedorService;

@RestController
@RequestMapping("/api/logistics/contenedores")
@Validated
public class ContenedorController {

    private final ContenedorService contenedorService;

    public ContenedorController(ContenedorService contenedorService) {
        this.contenedorService = contenedorService;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<PendingContainerResponse>> obtenerPendientes(
            @RequestParam(required = false) TramoEstado estado,
            @RequestParam(required = false) Long depositoId) {
        return ResponseEntity.ok(contenedorService.obtenerContenedoresPendientes(estado, depositoId));
    }
}
