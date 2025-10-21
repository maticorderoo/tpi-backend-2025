package com.tpibackend.distance.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * Configuración de WebClient para Google Directions API.
 * Incluye timeouts de conexión (5s) y respuesta (10s).
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient googleWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        return WebClient.builder()
                .baseUrl("https://maps.googleapis.com/maps/api/directions/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
