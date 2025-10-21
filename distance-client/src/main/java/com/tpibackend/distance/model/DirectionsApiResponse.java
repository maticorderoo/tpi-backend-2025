package com.tpibackend.distance.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectionsApiResponse {
    public List<Route> routes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        public List<Leg> legs;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {
        public ValueText distance;
        public ValueText duration;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValueText {
        public long value;   // metros (distance) o segundos (duration)
        public String text;
    }
}
