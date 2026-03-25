package com.lothuspay.gateway.filter;

import com.lothuspay.gateway.model.log.GatewayLog;
import com.lothuspay.gateway.repository.GatewayLogRepository;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.UUID;
@Component
@Order(0)
public class GatewayLoggingFilter implements GlobalFilter {

    private final GatewayLogRepository logRepository;

    public GatewayLoggingFilter(GatewayLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String clientIp = Optional.ofNullable(request.getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");

        GatewayLog log = GatewayLog.builder()
                .id(UUID.randomUUID().toString())
                .path(path)
                .method(method)
                .clientIp(clientIp)
                .timestamp(System.currentTimeMillis())
                .build();

        return chain.filter(exchange)
                .doOnTerminate(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.setCode(exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 0);
                    log.setDuration(duration);

                    Map<String, String> headers = new HashMap<>();
                    request.getHeaders().forEach((k, vv) -> headers.put(k, String.join(",", vv)));
                    log.setHeaders(headers);

                    // Salva o log assíncronamente
                    logRepository.save(log)
                            .doOnError(err -> System.err.println("Erro ao salvar log: " + err.getMessage()))
                            .subscribe();
                });
    }
}
