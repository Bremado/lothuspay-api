package com.lothuspay.payments.security;

import com.lothuspay.payments.pojo.UserContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(-1)
public class UserContextWebFilter implements WebFilter {

    public static final String USER_CONTEXT_KEY = "userContext";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String path = exchange.getRequest().getURI().getPath();

        var paths = new String[] { "/payments/callback/deposit", "/payments/callback/withdraw"};

        System.out.println("[JWT FILTER 1] - Processing request for path: " + path);

        if (Arrays.stream(paths).anyMatch(path::contains)) {
            return chain.filter(exchange);
        }

        if (headers.containsKey("X-Internal")) {
            return chain.filter(exchange);
        }

        System.out.println("[JWT FILTER] - Processing request for path: " + path);

        String userId = headers.getFirst("X-User-Id");
        String email = headers.getFirst("X-User-Email");
        List<String> roles = headers.getOrDefault("X-User-Roles", List.of());
        String segment = headers.getFirst("X-User-Segment");

        if (roles.size() == 1 && roles.get(0).contains(",")) {
            roles = Arrays.stream(roles.get(0).split(","))
                          .map(String::trim)
                          .collect(Collectors.toList());
        }

        System.out.println("[JWT FILTER] - Processing request for path: " + path);

        UserContext userContext = new UserContext(userId, email, roles, segment);

        if (userId == null || userId.isBlank()) {
            System.out.println("[JWT FILTER] - User ID is blank");
            System.out.println("UserID: " + userId + ", Email: " + email + ", Roles: " + Arrays.toString(roles.toArray()));
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange)
                    .contextWrite(ctx -> ctx.put(USER_CONTEXT_KEY, userContext));
    }
}
