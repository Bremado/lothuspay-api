package com.lothuspay.email.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleBasedAccessFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getHeaders().containsKey("X-Internal")) {
            return chain.filter(exchange);
        }

        List<String> roles = exchange.getRequest().getHeaders().get("X-User-Roles");

        if (roles == null || roles.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        boolean isManager = roles.stream().anyMatch(r -> r.contains("MANAGER"));
        boolean isCeo = roles.stream().anyMatch(r -> r.contains("CEO"));

        if (!isManager && !isCeo) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
