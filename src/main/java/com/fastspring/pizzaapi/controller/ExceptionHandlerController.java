package com.fastspring.pizzaapi.controller;

import com.fastspring.pizzaapi.dto.StandardResponse;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Slf4j
@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<StandardResponse<?>>> handler(BadCredentialsException ex) {
        Arrays.stream(ex.getStackTrace()).forEach(stack -> log.warn(String.valueOf(stack)));
        return Mono.just(new ResponseEntity<>(buildStandardResponse(ex.getMessage(), null), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<StandardResponse<?>>> handler(AccessDeniedException ex) {
        Arrays.stream(ex.getStackTrace()).forEach(stack -> log.warn(String.valueOf(stack)));
        return Mono.just(new ResponseEntity<>(buildStandardResponse(ex.getMessage(), null), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(SignatureException.class)
    public Mono<ResponseEntity<StandardResponse<?>>> handler(SignatureException ex) {
        Arrays.stream(ex.getStackTrace()).forEach(stack -> log.warn(String.valueOf(stack)));
        return Mono.just(new ResponseEntity<>(buildStandardResponse(ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<StandardResponse<?>>> handler(RuntimeException ex) {
        Arrays.stream(ex.getStackTrace()).forEach(stack -> log.warn(String.valueOf(stack)));
        return Mono.just(new ResponseEntity<>(buildStandardResponse(ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<StandardResponse<?>>> handler(Exception ex) {
        Arrays.stream(ex.getStackTrace()).forEach(stack -> log.warn(String.valueOf(stack)));
        return Mono.just(new ResponseEntity<>(buildStandardResponse(ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR));
    }


    private StandardResponse<?> buildStandardResponse(final String errorMessage, final String customMessage) {
        return new StandardResponse<>(
                null,
                customMessage,
                errorMessage
        );
    }
}
