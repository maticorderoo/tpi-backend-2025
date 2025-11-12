package com.tpibackend.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración de seguridad deshabilitada.
 * Ahora se usa SecurityConfig con seguridad completa en todos los entornos.
 */
@Configuration
public class DevSecurityConfig {
    // Security disabled - using main SecurityConfig for all profiles
}
