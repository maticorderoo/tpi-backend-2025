package com.tpibackend.orders.client;

import com.tpibackend.orders.dto.response.RutaResumenDto;
import com.tpibackend.orders.dto.response.TramoResumenDto;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class LogisticsClient {

    private static final Logger log = LoggerFactory.getLogger(LogisticsClient.class);

    private final WebClient webClient;
    private final String routeBySolicitudPath;

    public LogisticsClient(WebClient.Builder builder,
            @Value("${clients.logistics.base-url}") String baseUrl,
            @Value("${clients.logistics.route-by-solicitud-path}") String routeBySolicitudPath) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.routeBySolicitudPath = routeBySolicitudPath;
    }

    public Optional<RutaResumenDto> obtenerRutaPorSolicitud(Long solicitudId) {
        try {
            RutaResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(routeBySolicitudPath)
                            .build(solicitudId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(RutaResponse.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.empty())
                    .block();

            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(mapRuta(response));
        } catch (WebClientResponseException.NotFound ex) {
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("No se pudo obtener la ruta asociada a la solicitud {}: {}", solicitudId, ex.getMessage());
            return Optional.empty();
        }
    }

    private RutaResumenDto mapRuta(RutaResponse response) {
        List<TramoResumenDto> tramos = response.tramos() == null ? List.of()
                : response.tramos().stream().map(this::mapTramo).toList();
        return RutaResumenDto.builder()
                .id(response.id())
                .cantTramos(response.cantTramos())
                .cantDepositos(response.cantDepositos())
                .costoTotalAprox(response.costoTotalAprox())
                .costoTotalReal(response.costoTotalReal())
                .pesoTotal(response.pesoTotal())
                .volumenTotal(response.volumenTotal())
                .tramos(tramos)
                .build();
    }

    private TramoResumenDto mapTramo(TramoResponse tramo) {
        return TramoResumenDto.builder()
                .id(tramo.id())
                .rutaId(tramo.rutaId())
                .origenTipo(tramo.origenTipo())
                .origenId(tramo.origenId())
                .destinoTipo(tramo.destinoTipo())
                .destinoId(tramo.destinoId())
                .tipo(tramo.tipo())
                .estado(tramo.estado())
                .costoAprox(tramo.costoAprox())
                .costoReal(tramo.costoReal())
                .fechaHoraInicio(tramo.fechaHoraInicio())
                .fechaHoraFin(tramo.fechaHoraFin())
                .camionId(tramo.camionId())
                .distanciaKmEstimada(tramo.distanciaKmEstimada())
                .distanciaKmReal(tramo.distanciaKmReal())
                .diasEstadia(tramo.diasEstadia())
                .costoEstadiaDia(tramo.costoEstadiaDia())
                .costoEstadia(tramo.costoEstadia())
                .build();
    }

    private record RutaResponse(Long id, Long solicitudId, Integer cantTramos, Integer cantDepositos,
                                BigDecimal costoTotalAprox, BigDecimal costoTotalReal,
                                BigDecimal pesoTotal, BigDecimal volumenTotal,
                                List<TramoResponse> tramos) {
    }

    private record TramoResponse(Long id, Long rutaId, String origenTipo, Long origenId,
                                 String destinoTipo, Long destinoId, String tipo, String estado,
                                 BigDecimal costoAprox, BigDecimal costoReal,
                                 OffsetDateTime fechaHoraInicio, OffsetDateTime fechaHoraFin,
                                 Long camionId, Double distanciaKmEstimada, Double distanciaKmReal,
                                 Integer diasEstadia, BigDecimal costoEstadiaDia, BigDecimal costoEstadia) {
    }
}
