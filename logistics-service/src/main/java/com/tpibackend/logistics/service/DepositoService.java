package com.tpibackend.logistics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpibackend.logistics.dto.request.DepositoRequest;
import com.tpibackend.logistics.dto.response.DepositoResponse;
import com.tpibackend.logistics.model.Deposito;
import com.tpibackend.logistics.repository.DepositoRepository;

@Service
@Transactional
public class DepositoService {

    private final DepositoRepository depositoRepository;

    public DepositoService(DepositoRepository depositoRepository) {
        this.depositoRepository = depositoRepository;
    }

    public List<DepositoResponse> findAll() {
        return depositoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DepositoResponse findById(Long id) {
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado: " + id));
        return toResponse(deposito);
    }

    public DepositoResponse create(DepositoRequest request) {
        Deposito deposito = new Deposito();
        deposito.setNombre(request.nombre());
        deposito.setDireccion(request.direccion());
        deposito.setLat(request.lat());
        deposito.setLng(request.lng());
        deposito.setCostoEstadiaDia(request.costoEstadiaDia());

        Deposito saved = depositoRepository.save(deposito);
        return toResponse(saved);
    }

    public DepositoResponse update(Long id, DepositoRequest request) {
        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado: " + id));

        deposito.setNombre(request.nombre());
        deposito.setDireccion(request.direccion());
        deposito.setLat(request.lat());
        deposito.setLng(request.lng());
        deposito.setCostoEstadiaDia(request.costoEstadiaDia());

        Deposito updated = depositoRepository.save(deposito);
        return toResponse(updated);
    }

    private DepositoResponse toResponse(Deposito deposito) {
        return new DepositoResponse(
                deposito.getId(),
                deposito.getNombre(),
                deposito.getDireccion(),
                deposito.getLat(),
                deposito.getLng(),
                deposito.getCostoEstadiaDia()
        );
    }
}
