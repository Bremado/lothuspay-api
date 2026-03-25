package com.lothuspay.payments.security;

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
        String path = exchange.getRequest().getPath().toString();

        var paths = new String[] { "/payments/callback/deposit", "/payments/callback/withdraw"};

        if (Arrays.stream(paths).anyMatch(path::contains)) {
            return chain.filter(exchange);
        }

        if (exchange.getRequest().getHeaders().containsKey("X-Internal")) {
            return chain.filter(exchange);
        }

        List<String> roles = exchange.getRequest().getHeaders().get("X-User-Roles");

        if (roles == null || roles.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if (path.contains("/admin")) {
            boolean isManager = roles.stream().anyMatch(r -> r.contains("MANAGER"));
            boolean isCeo = roles.stream().anyMatch(r -> r.contains("CEO"));

            if (!isManager && !isCeo) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }
}
