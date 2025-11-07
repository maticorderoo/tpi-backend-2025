package com.tpibackend.orders.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OpenApiConfig {

    @Bean
    @Profile({"dev", "dev-docker"})
    public OpenAPI ordersOpenApiDev() {
        return new OpenAPI()
            .info(new Info()
                .title("Orders Service API")
                .description("API para la gestión de solicitudes de transporte de contenedores")
                .version("1.0"));
    }

    @Bean
    @Profile("!(dev | dev-docker)")
    public OpenAPI ordersOpenApiProd() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("Orders Service API")
                .description("API para la gestión de solicitudes de transporte de contenedores")
                .version("1.0"))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
