package com.lothuspay.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class InternalRouteFilter implements GlobalFilter, Ordered {

    @Value("${internal.secret}")
    private String internalToken;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        System.out.println("[InternalRouteFilter] Request path: " + path);
        // Só verificar rotas internas
        if (!path.startsWith("/int/")) {
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey("X-Internal")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = request.getHeaders().getFirst("X-Internal");

        if (!internalToken.equals(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        System.out.println("[InternalRouteFilter] Request Headers: " + request.getHeaders().toString());

        // 2. Remover qualquer header com esse nome
        ServerHttpRequest modifiedRequest = request.mutate()
                .headers(httpHeaders -> httpHeaders.remove("X-Internal"))
                .header("X-Internal", internalToken) // adicionar o válido
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return -10; // executar cedo
    }
}
