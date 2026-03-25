package com.lothuspay.payments.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient walletWebClient(@Value("${api.url}") String walletBaseUrl) {
        return WebClient.builder()
                .baseUrl(walletBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        .responseTimeout(Duration.ofSeconds(60))
                        .doOnConnected(conn ->
                                conn.addHandlerLast(new ReadTimeoutHandler(60))
                                        .addHandlerLast(new WriteTimeoutHandler(60))
                        )))
                .build();
    }

    @Bean
    public WebClient webClientMisticPay(
            @Value("${misticpay.ci}") String ci,
            @Value("${misticpay.cs}") String cs
    ) {
        return WebClient.builder()
                .baseUrl("https://api.misticpay.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("ci", ci)
                .defaultHeader("cs", cs)
                .build();
    }

    @Bean
    public WebClient webClientMisticPayBlack1(
            @Value("${misticpay.ci.black1}") String ci,
            @Value("${misticpay.cs.black1}") String cs
    ) {
        return WebClient.builder()
                .baseUrl("https://api.misticpay.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("ci", ci)
                .defaultHeader("cs", cs)
                .build();
    }

    @Bean
    public WebClient webClientMisticPayBlack2(
            @Value("${misticpay.ci.black2}") String ci,
            @Value("${misticpay.cs.black2}") String cs
    ) {
        return WebClient.builder()
                .baseUrl("https://api.misticpay.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("ci", ci)
                .defaultHeader("cs", cs)
                .build();
    }
}
