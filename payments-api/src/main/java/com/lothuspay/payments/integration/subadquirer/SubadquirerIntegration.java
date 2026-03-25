package com.lothuspay.payments.integration.subadquirer;

import com.fasterxml.jackson.core.JsonParser;
import com.lothuspay.payments.integration.subadquirer.dto.SubacquirerResponseDto;
import com.lothuspay.payments.integration.subadquirer.dto.SubacquirerRootResponseDto;
import com.lothuspay.payments.integration.subadquirer.dto.SubadquirerDepositRequestDto;
import com.lothuspay.payments.integration.subadquirer.dto.SubadquirerWithdrawRequestDto;
import com.lothuspay.payments.model.deposit.DepositRequest;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class SubadquirerIntegration {

    @Value("${misticpay.ci}")
    private String ci;

    @Value("${misticpay.cs}")
    private String cs;

    @Value("${misticpay.ci.black1}")
    private String blackCi;
    @Value("${misticpay.cs.black1}")
    private String blackCs;

    @Value("${misticpay.ci.black2}")
    private String blackCi2;
    @Value("${misticpay.cs.black2}")
    private String blackCs2;

    private final WebClient client;

    public SubadquirerIntegration() {
        this.client = WebClient.builder()
                .baseUrl("https://api.misticpay.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        .responseTimeout(Duration.ofSeconds(60))
                        .doOnConnected(conn ->
                                conn.addHandlerLast(new ReadTimeoutHandler(60))
                                        .addHandlerLast(new WriteTimeoutHandler(60))
                        )))
                .build();
    }

    public Mono<SubacquirerRootResponseDto> createDeposit(String segment, SubadquirerDepositRequestDto request) {
        var clientId = segment.equalsIgnoreCase("BLACKLABEL") ? blackCi :
                segment.equalsIgnoreCase("BLACKLABEL2") ? blackCi2 :
                ci;
        var clientSecret = segment.equalsIgnoreCase("BLACKLABEL") ? blackCs :
                segment.equalsIgnoreCase("BLACKLABEL2") ? blackCs2 :
                cs;

        return client.post()
                .uri("/api/transactions/create")
                .bodyValue(request)
                .header("ci", clientId)
                .header("cs", clientSecret)
                .retrieve()
                .bodyToMono(SubacquirerRootResponseDto.class);
    }

    public Mono<Object> createWithdraw(String segment, SubadquirerWithdrawRequestDto request) {
        var clientId = segment.equalsIgnoreCase("BLACKLABEL") ? blackCi :
                segment.equalsIgnoreCase("BLACKLABEL2") ? blackCi2 :
                ci;
        var clientSecret = segment.equalsIgnoreCase("BLACKLABEL") ? blackCs :
                segment.equalsIgnoreCase("BLACKLABEL2") ? blackCs2 :
                cs;

        return client.post()
                .uri("/api/transactions/withdraw")
                .bodyValue(request)
                .header("ci", clientId)
                .header("cs", clientSecret)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    System.out.println("[ERROR RESPONSE BODY] " + body);
                                    return Mono.empty();
                                })
                )
                .bodyToMono(Object.class)
                .doOnSuccess(responseDto -> System.out.println(responseDto.toString()))
                .doOnError(throwable -> {
                    System.out.println("Error during createWithdraw: " + throwable.getMessage());
                });
    }

}
