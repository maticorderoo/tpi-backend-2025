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
        validateTipo(request.tipo(), null);
        Tarifa tarifa = new Tarifa();
        tarifa.setTipo(request.tipo());
        tarifa.setValor(request.valor());
        Tarifa saved = tarifaRepository.save(tarifa);
        LOGGER.info("Tarifa creada con id {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public TarifaResponse update(Long id, TarifaRequest request) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarifa no encontrada"));
        validateTipo(request.tipo(), id);
        tarifa.setTipo(request.tipo());
        tarifa.setValor(request.valor());
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

    private void validateTipo(String tipo, Long id) {
        if (id == null && tarifaRepository.existsByTipoIgnoreCase(tipo)) {
            throw new BusinessException("Ya existe una tarifa con el tipo indicado");
        }
        if (id != null && tarifaRepository.existsByTipoIgnoreCaseAndIdNot(tipo, id)) {
            throw new BusinessException("Ya existe una tarifa con el tipo indicado");
        }
    }

    private TarifaResponse toResponse(Tarifa tarifa) {
        return new TarifaResponse(tarifa.getId(), tarifa.getTipo(), tarifa.getValor());
    }
}
