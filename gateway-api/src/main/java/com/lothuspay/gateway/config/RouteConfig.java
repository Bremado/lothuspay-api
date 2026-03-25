package com.lothuspay.gateway.config;

import com.lothuspay.gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Value("${internal.secret}")
    private String internalToken;

    public RouteConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        String authUrl = System.getenv("AUTH_SERVICE_URL");
        String walletUrl = System.getenv("WALLET_SERVICE_URL");
        String paymentsUrl = System.getenv("PAYMENTS_SERVICE_URL");
        String emailUrl = System.getenv("EMAIL_SERVICE_URL");

        return builder.routes()

                /* =========================
                   ROTAS PÚBLICAS COM JWT
                   ========================= */

                // Auth público
                .route("auth-service", r -> r.path("/v1/auth/**")
                        .filters(f -> f
                                .stripPrefix(1)      // /v1/auth -> /auth
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(authUrl))

                // Wallet público
                .route("wallet-service", r -> r.path("/v1/wallet/**")
                        .filters(f -> f
                                .stripPrefix(1)      // /v1/wallet -> /wallet
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(walletUrl))
                // Payments público
                .route("payments-service", r -> r.path("/v1/payments/**")
                        .filters(f -> f
                                .stripPrefix(1)      // /v1/payments -> /payments
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(paymentsUrl))
                .route("email-service", r -> r.path("/v1/email/**")
                        .filters(f -> f
                                .stripPrefix(1)      // /v1/email -> /email
                                .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(emailUrl))


                /* =========================
                   ROTAS INTERNAS DE SERVIÇO
                   ========================= */

                // Auth API interno
                .route("auth-internal", r -> r.path("/int/auth/internal/**")
                        .filters(f -> f
                                .stripPrefix(1) // remove /int
                                .addRequestHeader("X-Internal", internalToken)
                        )
                        .uri(authUrl))
                // Wallet API interno
                .route("wallet-internal", r -> r.path("/int/wallet/internal/**")
                        .filters(f -> f
                                .stripPrefix(1) // remove /int
                                .addRequestHeader("X-Internal", internalToken)
                        )
                        .uri(walletUrl))
                // Payments API interno
                .route("payments-internal", r -> r.path("/int/payments/internal/**")
                        .filters(f -> f
                                .stripPrefix(1) // remove /int
                                .addRequestHeader("X-Internal", internalToken)
                        )
                        .uri(paymentsUrl))

                .build();
    }
}
