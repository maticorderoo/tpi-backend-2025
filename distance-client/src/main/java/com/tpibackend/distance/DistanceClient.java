package com.tpibackend.distance;

import com.tpibackend.distance.model.DistanceResult;

public interface DistanceClient {

    DistanceResult getDistanceAndDuration(double originLat, double originLng,
            double destinationLat, double destinationLng);

    DistanceResult getDistanceAndDuration(double originLat, double originLng,
            double destinationLat, double destinationLng, String mode);

    DistanceResult getDistanceAndDuration(String origin, String destination);

    DistanceResult getDistanceAndDuration(String origin, String destination, String mode);
}
