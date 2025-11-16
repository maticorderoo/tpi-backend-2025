package com.tpibackend.logistics.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpibackend.logistics.dto.response.PendingContainerResponse;
import com.tpibackend.logistics.model.Deposito;
import com.tpibackend.logistics.model.Tramo;
import com.tpibackend.logistics.model.enums.LocationType;
import com.tpibackend.logistics.model.enums.TramoEstado;
import com.tpibackend.logistics.repository.DepositoRepository;
import com.tpibackend.logistics.repository.TramoRepository;

@Service
@Transactional(readOnly = true)
public class ContenedorService {

    // TODO: Servicio temporal; la consulta de contenedores debería vivir en Orders

    private final TramoRepository tramoRepository;
    private final DepositoRepository depositoRepository;

    public ContenedorService(TramoRepository tramoRepository, DepositoRepository depositoRepository) {
        this.tramoRepository = tramoRepository;
        this.depositoRepository = depositoRepository;
    }

    public List<PendingContainerResponse> obtenerContenedoresPendientes(TramoEstado estado, Long depositoId) {
        List<Tramo> tramos;
        if (estado != null) {
            tramos = tramoRepository.findByEstado(estado);
        } else {
            var estados = EnumSet.of(TramoEstado.ESTIMADO, TramoEstado.ASIGNADO, TramoEstado.INICIADO);
            tramos = tramoRepository.findByEstadoIn(List.copyOf(estados));
        }

        return tramos.stream()
                .filter(tramo -> tramo.getEstado() != TramoEstado.FINALIZADO)
                .filter(tramo -> depositoId == null || (tramo.getDestinoTipo() == LocationType.DEPOSITO
                        && depositoId.equals(tramo.getDestinoId())))
                .map(tramo -> {
                    Deposito destino = null;
                    if (tramo.getDestinoTipo() == LocationType.DEPOSITO && tramo.getDestinoId() != null) {
                        destino = depositoRepository.findById(tramo.getDestinoId()).orElse(null);
                    }
                    String descripcionDestino = Optional.ofNullable(destino)
                            .map(Deposito::getDireccion)
                            .orElse(tramo.getDestinoTipo() != null ? tramo.getDestinoTipo().name() : "DESTINO");
                    return new PendingContainerResponse(
                            tramo.getRuta() != null ? tramo.getRuta().getSolicitudId() : null,
                            tramo.getRuta() != null ? tramo.getRuta().getId() : null,
                            tramo.getId(),
                            tramo.getEstado(),
                            destino != null ? destino.getId() : null,
                            destino != null ? destino.getNombre() : null,
                            descripcionDestino);
                })
                .toList();
    }
}
