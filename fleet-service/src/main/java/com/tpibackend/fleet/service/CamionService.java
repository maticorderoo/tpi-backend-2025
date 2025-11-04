package com.tpibackend.fleet.service;

import com.tpibackend.fleet.model.dto.CamionAvailabilityRequest;
import com.tpibackend.fleet.model.dto.CamionRequest;
import com.tpibackend.fleet.model.dto.CamionResponse;
import java.util.List;

public interface CamionService {

    List<CamionResponse> findAll(Boolean disponible);

    CamionResponse findById(Long id);

    CamionResponse create(CamionRequest request);

    CamionResponse update(Long id, CamionRequest request);

    CamionResponse updateAvailability(Long id, CamionAvailabilityRequest request);
}
