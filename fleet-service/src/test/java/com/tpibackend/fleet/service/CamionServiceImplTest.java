package com.tpibackend.fleet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpibackend.fleet.exception.BusinessException;
import com.tpibackend.fleet.exception.ResourceNotFoundException;
import com.tpibackend.fleet.model.dto.CamionAvailabilityRequest;
import com.tpibackend.fleet.model.dto.CamionRequest;
import com.tpibackend.fleet.model.dto.CamionResponse;
import com.tpibackend.fleet.model.entity.Camion;
import com.tpibackend.fleet.repository.CamionRepository;
import com.tpibackend.fleet.service.impl.CamionServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CamionServiceImplTest {

    @Mock
    private CamionRepository camionRepository;

    @InjectMocks
    private CamionServiceImpl camionService;

    private CamionRequest buildRequest() {
        return new CamionRequest("ABC123", "Transportista", "123456789",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(30), true,
                BigDecimal.valueOf(120), BigDecimal.valueOf(5));
    }

    @Test
    void createCamion_whenDominioExists_shouldThrowBusinessException() {
        CamionRequest request = buildRequest();
        when(camionRepository.existsByDominioIgnoreCase(request.dominio())).thenReturn(true);

        assertThrows(BusinessException.class, () -> camionService.create(request));
    }

    @Test
    void createCamion_shouldPersistAndReturnResponse() {
        CamionRequest request = buildRequest();
        when(camionRepository.existsByDominioIgnoreCase(request.dominio())).thenReturn(false);
        when(camionRepository.save(any(Camion.class))).thenAnswer(invocation -> {
            Camion camion = invocation.getArgument(0);
            camion.setId(1L);
            return camion;
        });

        CamionResponse response = camionService.create(request);

        assertEquals(1L, response.id());
        assertEquals(request.dominio(), response.dominio());
        verify(camionRepository).save(any(Camion.class));
    }

    @Test
    void updateAvailability_whenCamionNotFound_shouldThrow() {
        when(camionRepository.findById(1L)).thenReturn(Optional.empty());
        CamionAvailabilityRequest request = new CamionAvailabilityRequest(false);

        assertThrows(ResourceNotFoundException.class, () -> camionService.updateAvailability(1L, request));
    }

    @Test
    void updateAvailability_shouldUpdateCamion() {
        Camion camion = new Camion();
        camion.setId(1L);
        camion.setDominio("ABC123");
        camion.setTransportistaNombre("Transportista");
        camion.setTelefono("123456789");
        camion.setCapPeso(BigDecimal.valueOf(1000));
        camion.setCapVolumen(BigDecimal.valueOf(30));
        camion.setDisponible(true);
        camion.setCostoKmBase(BigDecimal.valueOf(120));
        camion.setConsumoLKm(BigDecimal.valueOf(5));

        when(camionRepository.findById(1L)).thenReturn(Optional.of(camion));
        when(camionRepository.save(camion)).thenReturn(camion);

        CamionAvailabilityRequest request = new CamionAvailabilityRequest(false);
        CamionResponse response = camionService.updateAvailability(1L, request);

        assertFalse(response.disponible());
        verify(camionRepository).save(camion);
    }
}
