package com.tpibackend.gateway.filter;

import java.security.Principal;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Propaga información básica del usuario autenticado a los microservicios para facilitar trazabilidad.
 */
@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-User-Username";
    private static final String HEADER_ROLES = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .defaultIfEmpty(null)
                .flatMap(principal -> chain.filter(exchange.mutate()
                        .request(augmentRequest(exchange.getRequest(), principal))
                        .build()));
    }

    private ServerHttpRequest augmentRequest(ServerHttpRequest request, Principal principal) {
        if (!(principal instanceof Authentication authentication) ||
                !(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return request;
        }

        ServerHttpRequest.Builder builder = request.mutate();
        String subject = jwtAuth.getToken().getSubject();
        String preferredUsername = jwtAuth.getToken().getClaimAsString("preferred_username");
        String email = jwtAuth.getToken().getClaimAsString("email");
        String username = preferredUsername != null ? preferredUsername : email;
        if (subject != null) {
            builder.header(HEADER_USER_ID, subject);
        }
        if (username != null) {
            builder.header(HEADER_USERNAME, username);
        }
        String roles = authentication.getAuthorities().stream()
                .map(granted -> granted.getAuthority().replace("ROLE_", ""))
                .sorted()
                .collect(Collectors.joining(","));
        if (!roles.isEmpty()) {
            builder.header(HEADER_ROLES, roles);
        }
        return builder.build();
    }

    @Override
    public int getOrder() {
        // Debe ejecutarse luego de la autenticación para disponer del SecurityContext.
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
