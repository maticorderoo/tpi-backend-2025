package com.tpibackend.distance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tpibackend.distance.model.DistanceData;

@Component
public class DistanceClient {

    private static final Logger log = LoggerFactory.getLogger(DistanceClient.class);
    private static final double EARTH_RADIUS_KM = 6371.0;

    public DistanceData getDistance(double fromLat, double fromLng, double toLat, double toLng) {
        return getDistance(fromLat, fromLng, toLat, toLng, "driving");
    }

    public DistanceData getDistance(double fromLat, double fromLng, double toLat, double toLng, String mode) {
        double distanceKm = haversine(fromLat, fromLng, toLat, toLng);
        double averageSpeedKmH = switch (mode) {
            case "walking" -> 5d;
            case "bicycling" -> 15d;
            case "transit" -> 40d;
            default -> 60d;
        };
        double durationMinutes = distanceKm == 0 ? 0 : (distanceKm / averageSpeedKmH) * 60d;
        log.debug("Distancia calculada localmente: {} km en modo {}", distanceKm, mode);
        return new DistanceData(distanceKm, durationMinutes);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(rLat1) * Math.cos(rLat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS_KM * c;
    }
}
