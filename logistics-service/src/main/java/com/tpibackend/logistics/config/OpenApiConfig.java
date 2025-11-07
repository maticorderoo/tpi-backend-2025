package com.tpibackend.logistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    @Profile({"dev", "dev-docker"})
    public OpenAPI logisticsOpenAPIDev() {
        return new OpenAPI()
                .info(new Info()
                        .title("Logistics Service API")
                        .description("Gestión de rutas, tramos y depósitos")
                        .version("v1"));
    }

    @Bean
    @Profile("!(dev | dev-docker)")
    public OpenAPI logisticsOpenAPIProd() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Logistics Service API")
                        .description("Gestión de rutas, tramos y depósitos")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
