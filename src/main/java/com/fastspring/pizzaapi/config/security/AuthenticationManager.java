package com.fastspring.pizzaapi.config.security;

import com.fastspring.pizzaapi.dto.auth.BearerToken;
import com.fastspring.pizzaapi.service.auth.JwtService;
import com.fastspring.pizzaapi.service.auth.JwtServiceImpl;
import com.fastspring.pizzaapi.service.auth.UserDetailsService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final UserDetailsService userDetailsService;

    private final JwtService jwtService;

    public AuthenticationManager(final UserDetailsService userDetailsService,
                                 final JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(final Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .cast(BearerToken.class)
                .map(BearerToken::getToken)
                .flatMap(this::processToken);
    }

    private Mono<Authentication> processToken(String token) {
        try {
            final String userName = jwtService.extractUsername(token);
            return userDetailsService.findByUsername(userName)
                    .map(Optional::of)
                    .flatMap(userDetailsOptional -> validateCredentials(token, userDetailsOptional));
        }catch (RuntimeException e) {
            return Mono.error(new BadCredentialsException(e.getMessage()));
        }
    }

    private Mono<Authentication> validateCredentials(String token, Optional<UserDetails> userDetailsOptional) {
        if(userDetailsOptional.isPresent()) {
            final UserDetails userDetails = userDetailsOptional.get();
            if(jwtService.validateToken(token, userDetails)) {
                return Mono.just(new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities()));
            }
            return Mono.error(new BadCredentialsException("Invalid token"));
        }else {
            return Mono.error(new BadCredentialsException("No user matches credentials"));
        }
    }
}
