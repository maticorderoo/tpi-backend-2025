package com.tpibackend.fleet.service;

import com.tpibackend.fleet.model.dto.TarifaRequest;
import com.tpibackend.fleet.model.dto.TarifaResponse;
import java.util.List;

public interface TarifaService {

    List<TarifaResponse> findAll();

    TarifaResponse create(TarifaRequest request);

    TarifaResponse update(Long id, TarifaRequest request);

    void delete(Long id);

    TarifaResponse obtenerActiva();
}
