package com.lothuspay.gateway.filter;

import com.lothuspay.gateway.dto.InternalAuthData;
import com.lothuspay.gateway.dto.InternalAuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class ApiKeyFilter implements GlobalFilter, Ordered {

    @Value("${internal.secret}")
    private String internalToken;

    private final WebClient webClient;

    public ApiKeyFilter(@Qualifier("authClient") WebClient authClient) {
        super();
        this.webClient = authClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        System.out.println("ApiKeyFilter: " + path);

        if (path.contains("/int")) {
            return chain.filter(exchange);
        }

        var paths = new String[]{
                "/v1/auth/login",
                "/v1/auth/register",
                "/v1/auth/validate",
                "/auth/login",
                "/auth/register",
                "/auth/validate",
                "/payments/callback/deposit",
                "/auth/profile/2fa/verify",
                "/payments/callback/withdraw",
                "/v1/payments/callback/deposit",
                "/v1/payments/callback/withdraw"
        };


        if (Arrays.stream(paths).anyMatch(path::contains)) {
            System.out.println("[JWT FILTER] Rota pública detectada: " + path);
            return chain.filter(exchange);
        }


        String apiKey = request.getHeaders().getFirst("X-Api-Key");
        if (apiKey == null) {
            if (!request.getHeaders().containsKey("Authorization")) {
                return unauthorized(exchange, "Missing API Key");
            }
            return chain.filter(exchange);
        }

        String[] parts = apiKey.split("\\.");
        if (parts.length != 2) {
            return unauthorized(exchange, "Invalid API Key format");
        }

        String clientId = parts[0];
        String secret = parts[1];

        return webClient.post()
                .uri("/auth/internal/apikey/validate")
                .header("X-Internal", internalToken)
                .header("X-Client-ID", clientId)
                .header("X-Client-Secret", secret)
                .retrieve()
                .bodyToMono(InternalAuthResponse.class)
                .flatMap(response -> {
                    if (!"SUCCESS".equals(response.getStatus())) {
                        return unauthorized(exchange, response.getMessage());
                    }

                    InternalAuthData data = response.getData();

                    ServerHttpRequest mutated = request.mutate()
                            .header("X-Client-Id", data.getClientId())
                            .header("X-User-Id", data.getUserId())
                            .header("X-User-Roles", data.getRoles().toArray(new String[0]))
                            .header("X-User-Segment", data.getSegment())
                            .build();

                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return unauthorized(exchange, "Error validating API Key");
                });
    }

    @Override
    public int getOrder() {
        return -5;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

}
