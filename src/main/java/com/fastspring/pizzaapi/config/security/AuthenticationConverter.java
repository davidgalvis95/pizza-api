package com.fastspring.pizzaapi.config.security;

import com.fastspring.pizzaapi.dto.auth.BearerToken;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerHttpBasicAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@NoArgsConstructor
public class AuthenticationConverter extends ServerHttpBasicAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .flatMap(this::extractBearerToken)
                .map(BearerToken::new);
    }

    private Mono<String> extractBearerToken(String rawToken) {
        return rawToken.startsWith("Bearer") ? Mono.just(rawToken.substring(7)) : Mono.empty();
    }
}
