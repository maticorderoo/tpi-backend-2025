package com.tpibackend.fleet.service.impl;

import com.tpibackend.fleet.exception.BusinessException;
import com.tpibackend.fleet.exception.ResourceNotFoundException;
import com.tpibackend.fleet.model.dto.TarifaRequest;
import com.tpibackend.fleet.model.dto.TarifaResponse;
import com.tpibackend.fleet.model.entity.Tarifa;
import com.tpibackend.fleet.repository.TarifaRepository;
import com.tpibackend.fleet.service.TarifaService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TarifaServiceImpl implements TarifaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TarifaServiceImpl.class);

    private final TarifaRepository tarifaRepository;

    public TarifaServiceImpl(TarifaRepository tarifaRepository) {
        this.tarifaRepository = tarifaRepository;
    }

    @Override
    public List<TarifaResponse> findAll() {
        return tarifaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TarifaResponse create(TarifaRequest request) {
        validateNombre(request.nombre(), null);
        Tarifa tarifa = new Tarifa();
        tarifa.setNombre(request.nombre());
        tarifa.setCostoKm(request.costoKm());
        tarifa.setCostoHora(request.costoHora());
        tarifa.setMoneda(request.moneda());
        Tarifa saved = tarifaRepository.save(tarifa);
        LOGGER.info("Tarifa creada con id {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public TarifaResponse update(Long id, TarifaRequest request) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa no encontrada"));
        validateNombre(request.nombre(), id);
        tarifa.setNombre(request.nombre());
        tarifa.setCostoKm(request.costoKm());
        tarifa.setCostoHora(request.costoHora());
        tarifa.setMoneda(request.moneda());
        Tarifa saved = tarifaRepository.save(tarifa);
        LOGGER.info("Tarifa actualizada con id {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa no encontrada"));
        tarifaRepository.delete(tarifa);
        LOGGER.info("Tarifa eliminada con id {}", tarifa.getId());
    }

    @Override
    public TarifaResponse obtenerActiva() {
        Tarifa tarifa = tarifaRepository.findTopByOrderByCreatedAtDescIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No hay tarifas configuradas"));
        return toResponse(tarifa);
    }

    private void validateNombre(String nombre, Long id) {
        if (id == null && tarifaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe una tarifa con el nombre indicado");
        }
        if (id != null && tarifaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new BusinessException("Ya existe una tarifa con el nombre indicado");
        }
    }

    private TarifaResponse toResponse(Tarifa tarifa) {
        return new TarifaResponse(
                tarifa.getId(),
                tarifa.getNombre(),
                tarifa.getCostoKm(),
                tarifa.getCostoHora(),
                tarifa.getMoneda(),
                tarifa.getCreatedAt());
    }
}
