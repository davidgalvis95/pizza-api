package com.fastspring.pizzaapi.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastspring.pizzaapi.dto.StandardResponse;
import lombok.NoArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class AuthFailureHandler {
    public Mono<Void> formatResponse(ServerHttpResponse response, String message) {
        response.getHeaders()
                .add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        final ObjectMapper mapper = new ObjectMapper();
        final StandardResponse<?> standardResponse = StandardResponse.builder()
                .error(message)
                .build();

        final StringBuilder json = new StringBuilder();
        try {
            json.append(mapper.writeValueAsString(standardResponse));
        } catch (JsonProcessingException jsonProcessingException) {
            return Mono.error(new RuntimeException("Cannot process authentication exception"));
        }

        final String responseBody = json.toString();
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        final DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
