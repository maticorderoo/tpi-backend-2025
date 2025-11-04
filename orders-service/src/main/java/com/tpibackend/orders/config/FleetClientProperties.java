package com.tpibackend.orders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients.fleet")
public record FleetClientProperties(String baseUrl, String metricsPath) {
}
