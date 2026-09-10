package com.example.gateway_service.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Единственная точка, которой можно доверять заголовки X-Lichnost-Id/X-User-Role/
 * X-School-Id — downstream-сервисы читают их напрямую, не перепроверяя. Поэтому
 * здесь: (1) всегда стираем то, что прислал клиент, (2) если есть Authorization —
 * подтверждаем токен интроспекцией в auth_service и подставляем свои заголовки,
 * (3) без Authorization — пропускаем без identity (публичные эндпоинты вроде login
 * или просмотра каталога).
 *
 * Известное ограничение: сервисы всё ещё доступны напрямую по своим портам в обход
 * Gateway (нет сетевой изоляции) — для локальной разработки это нормально, для
 * прод-деплоя потребуется закрыть прямой доступ снаружи.
 */
@Component
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final String HEADER_LICHNOST_ID = "X-Lichnost-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_SCHOOL_ID = "X-School-Id";

    private final WebClient webClient;

    public AuthenticationGlobalFilter(@Value("${auth-service.base-url}") String authServiceBaseUrl) {
        this.webClient = WebClient.builder().baseUrl(authServiceBaseUrl).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        ServerHttpRequest strippedRequest = request.mutate()
                .headers(headers -> {
                    headers.remove(HEADER_LICHNOST_ID);
                    headers.remove(HEADER_USER_ROLE);
                    headers.remove(HEADER_SCHOOL_ID);
                })
                .build();

        if (authHeader == null || authHeader.isBlank()) {
            return chain.filter(exchange.mutate().request(strippedRequest).build());
        }

        WebClient.RequestHeadersUriSpec<?> requestSpec = webClient.get();
        WebClient.RequestHeadersSpec<?> uriSpec = requestSpec.uri("/api/v1/auth/verify");
        WebClient.RequestHeadersSpec<?> headersSpec = uriSpec.header(HttpHeaders.AUTHORIZATION, authHeader);
        WebClient.ResponseSpec responseSpec = headersSpec.retrieve();
        Mono<VerifyResponse> verifyMono = responseSpec.bodyToMono(VerifyResponse.class);

        return verifyMono
                .flatMap(verify -> {
                    ServerHttpRequest authenticatedRequest = strippedRequest.mutate()
                            .headers(headers -> {
                                headers.set(HEADER_LICHNOST_ID, String.valueOf(verify.lichnostId()));
                                headers.set(HEADER_USER_ROLE, verify.role());
                                if (verify.schoolId() != null) {
                                    headers.set(HEADER_SCHOOL_ID, String.valueOf(verify.schoolId()));
                                }
                            })
                            .build();
                    return chain.filter(exchange.mutate().request(authenticatedRequest).build());
                })
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private record VerifyResponse(long lichnostId, String role, Long schoolId, long expiresAtEpochSeconds) {
    }
}
