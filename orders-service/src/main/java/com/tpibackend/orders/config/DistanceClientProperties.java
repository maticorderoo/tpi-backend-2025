package com.tpibackend.orders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients.distance")
public record DistanceClientProperties(String baseUrl, String routePath) {
}
