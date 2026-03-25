package com.lothuspay.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    @Bean
    public WebClient authClient(WebClient.Builder webClientBuilder) {
        var authUrl = System.getenv("AUTH_SERVICE_URL");
        return webClientBuilder
                .baseUrl(authUrl)
                .build();
    }
}
