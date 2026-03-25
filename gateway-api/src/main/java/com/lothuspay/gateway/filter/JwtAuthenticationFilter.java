package com.lothuspay.gateway.filter;

import com.lothuspay.gateway.dto.ValidateTokenRequest;
import com.lothuspay.gateway.dto.ValidateTokenResponse;
import com.lothuspay.gateway.provider.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;

@Component
@Order(0)
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final WebClient webClient;

    public JwtAuthenticationFilter(@Qualifier("authClient") WebClient authClient) {
        super(Config.class);
        this.webClient = authClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            var path = request.getPath().value();

            System.out.println("[JWT FILTER] Nova requisição recebida: " + path);


            if (request.getHeaders().containsKey("X-Api-Key")) {
                System.out.println("[JWT FILTER] Rota com X-Api-Key detectada: " + path);
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
                    "/payments/callback/withdraw",
                    "/auth/profile/2fa/verify",
                    "/v1/payments/callback/deposit",
                    "/v1/payments/callback/withdraw"
            };

            if (path.contains("/system/config") && request.getMethod().matches("GET")) {
                System.out.println("[JWT FILTER] Rota pública detectada: " + path);
                return chain.filter(exchange);
            }

            if (Arrays.stream(paths).anyMatch(path::contains)) {
                System.out.println("[JWT FILTER] Rota pública detectada: " + path);
                return chain.filter(exchange);
            }


            var authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || authHeader.isBlank()) {
                System.out.println("[JWT FILTER] Nenhum header Authorization encontrado.");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            if (!authHeader.startsWith("Bearer ")) {
                System.out.println("[JWT FILTER] Header inválido. Deve começar com Bearer.");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            var token = authHeader.substring(7);

            System.out.println("[JWT FILTER] Enviando token para validação no Auth API...");

            return webClient.post()
                    .uri("/auth/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new ValidateTokenRequest(token))
                    .retrieve()
                    .bodyToMono(ValidateTokenResponse.class)
                    .flatMap(res -> {
                        if (!res.getStatus().equalsIgnoreCase("SUCCESS")) {
                            System.out.println("[JWT FILTER] Token recusado pelo Auth API.");
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        ServerWebExchange mutated = exchange.mutate()
                                .request(r -> r.headers(h -> {
                                    h.add("X-User-Id", res.getData().getUserId());
                                    h.add("X-User-Email", res.getData().getEmail());
                                    h.add("X-User-Roles", String.join(",", res.getData().getRoles()));
                                    h.add("X-User-Verified", String.valueOf(res.getData().isVerified()));
                                    h.add("X-User-Email-Verified", String.valueOf(res.getData().isEmailVerified()));
                                    h.add("X-User-Segment", res.getData().getSegment());
                                    h.set("Forwarded", "gateway");
                                }))
                                .build();

                        System.out.println("[JWT FILTER] Token validado. Encaminhando requisição...");
                        return chain.filter(mutated);
                    })
                    .onErrorResume(err -> {
                        System.out.println("[JWT FILTER] " + err.getMessage());
                        System.out.println("[JWT FILTER] Erro chamando Auth API: " + err.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }


    public static class Config {}
}
