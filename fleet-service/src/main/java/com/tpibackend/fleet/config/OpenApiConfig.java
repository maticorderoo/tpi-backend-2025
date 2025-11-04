package com.tpibackend.fleet.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Fleet Service API", version = "1.0", description = "Gestión de camiones y tarifas"),
        servers = {@Server(url = "/", description = "Default Server")}
)
public class OpenApiConfig {
}
