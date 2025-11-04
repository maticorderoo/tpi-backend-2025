package com.tpibackend.fleet.service.impl;

import com.tpibackend.fleet.exception.BusinessException;
import com.tpibackend.fleet.exception.ResourceNotFoundException;
import com.tpibackend.fleet.model.dto.CamionAvailabilityRequest;
import com.tpibackend.fleet.model.dto.CamionRequest;
import com.tpibackend.fleet.model.dto.CamionResponse;
import com.tpibackend.fleet.model.entity.Camion;
import com.tpibackend.fleet.repository.CamionRepository;
import com.tpibackend.fleet.service.CamionService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class CamionServiceImpl implements CamionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamionServiceImpl.class);

    private final CamionRepository camionRepository;

    public CamionServiceImpl(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    @Override
    public List<CamionResponse> findAll(Boolean disponible) {
        List<Camion> camiones;
        if (disponible == null) {
            camiones = camionRepository.findAll();
        } else if (Boolean.TRUE.equals(disponible)) {
            camiones = camionRepository.findByDisponibleTrue();
        } else {
            camiones = camionRepository.findByDisponibleFalse();
        }
        return camiones.stream().map(this::toResponse).toList();
    }

    @Override
    public CamionResponse findById(Long id) {
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camión no encontrado"));
        return toResponse(camion);
    }

    @Override
    public CamionResponse create(CamionRequest request) {
        validateDominio(request.dominio(), null);
        Camion camion = mapToEntity(request, new Camion());
        Camion saved = camionRepository.save(camion);
        LOGGER.info("Camión creado con id {} y dominio {}", saved.getId(), saved.getDominio());
        return toResponse(saved);
    }

    @Override
    public CamionResponse update(Long id, CamionRequest request) {
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camión no encontrado"));
        validateDominio(request.dominio(), id);
        mapToEntity(request, camion);
        Camion saved = camionRepository.save(camion);
        LOGGER.info("Camión actualizado con id {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public CamionResponse updateAvailability(Long id, CamionAvailabilityRequest request) {
        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camión no encontrado"));
        camion.setDisponible(request.disponible());
        Camion saved = camionRepository.save(camion);
        LOGGER.info("Disponibilidad actualizada para camión {} -> {}", saved.getId(), saved.getDisponible());
        return toResponse(saved);
    }

    private void validateDominio(String dominio, Long id) {
        if (!StringUtils.hasText(dominio)) {
            throw new BusinessException("El dominio es obligatorio");
        }
        if (id == null && camionRepository.existsByDominioIgnoreCase(dominio)) {
            throw new BusinessException("Ya existe un camión con el dominio indicado");
        }
        if (id != null && camionRepository.existsByDominioIgnoreCaseAndIdNot(dominio, id)) {
            throw new BusinessException("Ya existe un camión con el dominio indicado");
        }
    }

    private Camion mapToEntity(CamionRequest request, Camion camion) {
        camion.setDominio(request.dominio().toUpperCase());
        camion.setTransportistaNombre(request.transportistaNombre());
        camion.setTelefono(request.telefono());
        camion.setCapPeso(request.capPeso());
        camion.setCapVolumen(request.capVolumen());
        camion.setDisponible(Boolean.TRUE.equals(request.disponible()));
        camion.setCostoKmBase(request.costoKmBase());
        camion.setConsumoLKm(request.consumoLKm());
        return camion;
    }

    private CamionResponse toResponse(Camion camion) {
        return new CamionResponse(camion.getId(), camion.getDominio(), camion.getTransportistaNombre(),
                camion.getTelefono(), camion.getCapPeso(), camion.getCapVolumen(), camion.getDisponible(),
                camion.getCostoKmBase(), camion.getConsumoLKm());
    }
}
