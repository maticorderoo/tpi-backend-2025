package com.tpibackend.distance.mapper;

import com.tpibackend.distance.model.DirectionsApiResponse;
import com.tpibackend.distance.model.DistanceResult;

/**
 * Mapper para convertir respuestas de Google Directions API a DistanceResult.
 * Suma distancias y duraciones de todos los legs (tramos) de la ruta.
 */
public class DistanceResponseMapper {

    /**
     * Convierte una respuesta de Google Directions en DistanceResult.
     * 
     * @param response respuesta de la API (no debe ser null)
     * @return DistanceData con km y minutos totales
     * @throws IllegalArgumentException si la respuesta no tiene rutas válidas
     */
    public static DistanceResult from(DirectionsApiResponse response) {
        if (response == null || response.routes == null || response.routes.isEmpty()) {
            throw new IllegalArgumentException("La respuesta no contiene rutas válidas");
        }

        var firstRoute = response.routes.get(0);
        if (firstRoute.legs == null || firstRoute.legs.isEmpty()) {
            throw new IllegalArgumentException("La ruta no contiene legs (tramos) válidos");
        }

        // Sumar todos los tramos
        long totalMeters = firstRoute.legs.stream()
                .mapToLong(leg -> leg.distance.value)
                .sum();

        long totalSeconds = firstRoute.legs.stream()
                .mapToLong(leg -> leg.duration.value)
                .sum();

        double distanceKm = totalMeters / 1000.0;
        double durationMinutes = totalSeconds / 60.0;

        return new DistanceResult(distanceKm, durationMinutes);
    }
}
