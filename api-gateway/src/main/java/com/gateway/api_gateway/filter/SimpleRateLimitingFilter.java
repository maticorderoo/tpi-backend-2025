package com.gateway.api_gateway.filter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SimpleRateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(SimpleRateLimitingFilter.class);
    private static final int MAX_REQUESTS_PER_WINDOW = 100;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    private final Map<String, RequestQuota> quotas = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String key = resolveKey(exchange.getRequest());
        RequestQuota quota = quotas.compute(key, (k, existing) -> updateQuota(existing));

        if (quota.getCount() > MAX_REQUESTS_PER_WINDOW) {
            log.debug("Rate limit exceeded for key {}", key);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().set("Retry-After", String.valueOf(WINDOW_DURATION.getSeconds()));
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private String resolveKey(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authorization)) {
            return authorization;
        }
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "anonymous";
    }

    private RequestQuota updateQuota(RequestQuota existing) {
        Instant now = Instant.now();
        if (existing == null || now.isAfter(existing.windowStart().plus(WINDOW_DURATION))) {
            return new RequestQuota(now, 1);
        }

        int updatedCount = existing.count() + 1;
        return new RequestQuota(existing.windowStart(), updatedCount);
    }

    private record RequestQuota(Instant windowStart, int count) {
        int getCount() {
            return count;
        }
    }
}
