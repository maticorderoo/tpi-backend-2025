package com.tpibackend.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.tpibackend.gateway.security.KeycloakRealmRoleConverter;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final String ROLE_CLIENTE = "CLIENTE";
    private static final String ROLE_OPERADOR = "OPERADOR";
    private static final String ROLE_TRANSPORTISTA = "TRANSPORTISTA";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers(
                                "/api/*/swagger-ui/**",
                                "/api/*/swagger-ui.html",
                                "/api/*/v3/api-docs/**",
                                "/api/*/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .pathMatchers("/api/orders/orders/internal/**").denyAll()
                        .pathMatchers(HttpMethod.POST, "/api/orders/orders").hasRole(ROLE_CLIENTE)
                        .pathMatchers(HttpMethod.GET, "/api/orders/orders/*/tracking").hasAnyRole(ROLE_CLIENTE, ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.GET, "/api/orders/orders/*").hasAnyRole(ROLE_CLIENTE, ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.PUT, "/api/orders/orders/*/costo").hasRole(ROLE_OPERADOR)
                        .pathMatchers("/api/orders/orders/containers/**").hasRole(ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.GET, "/api/logistics/seguimiento/pendientes").hasRole(ROLE_OPERADOR)
                        .pathMatchers("/api/logistics/seguimiento/contenedores/**").hasAnyRole(ROLE_CLIENTE, ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.POST, "/api/logistics/tramos/*/asignaciones", "/api/logistics/tramos/*/asignar-camion")
                            .hasRole(ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.POST, "/api/logistics/tramos/*/inicios", "/api/logistics/tramos/*/finalizaciones")
                            .hasAnyRole(ROLE_TRANSPORTISTA, ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.GET, "/api/logistics/tramos/**")
                            .hasAnyRole(ROLE_OPERADOR, ROLE_TRANSPORTISTA)
                        .pathMatchers("/api/logistics/rutas/**").hasRole(ROLE_OPERADOR)
                        .pathMatchers("/api/logistics/depositos/**").hasRole(ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.GET, "/api/fleet/trucks/**").hasRole(ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.POST, "/api/fleet/trucks/**").hasRole(ROLE_OPERADOR)
                        .pathMatchers(HttpMethod.PUT, "/api/fleet/trucks/**").hasRole(ROLE_OPERADOR)
                        .pathMatchers("/api/fleet/tarifas/**").hasRole(ROLE_OPERADOR)
                        .pathMatchers("/api/fleet/metrics/**").hasRole(ROLE_OPERADOR)
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
