package com.fastspring.pizzaapi.integration;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.auth.LoginRequest;
import com.fastspring.pizzaapi.dto.auth.LoginResponse;
import com.fastspring.pizzaapi.dto.auth.SignUpResponse;
import com.fastspring.pizzaapi.dto.auth.SignupRequest;
import com.fastspring.pizzaapi.model.User;
import com.fastspring.pizzaapi.model.enums.Role;
import com.fastspring.pizzaapi.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@NoArgsConstructor
public class AuthenticationHelper {

    public String registerIfNotExistsAndLoginUser(final WebTestClient webTestClient,
                                                  final UserRepository userRepository,
                                                  final String userEmail,
                                                  final String userPassword,
                                                  final Set<Role> roles) {
        final Optional<User> existentUserOptional = Optional.ofNullable(userRepository.findByEmail(userEmail).block());

        if (existentUserOptional.isEmpty()) {
            final SignupRequest signupRequest = new SignupRequest(
                    userEmail,
                    userPassword,
                    roles
            );

            final SignUpResponse expectedSignUpResponse = SignUpResponse.builder()
                    .roles(signupRequest.getRoles())
                    .email(signupRequest.getEmail())
                    .build();

            final Flux<StandardResponse<SignUpResponse>> signUpResponse = webTestClient.post()
                    .uri("/api/v1/user/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(signupRequest))
                    .exchange()
                    .expectStatus().isAccepted()
                    .returnResult(new ParameterizedTypeReference<StandardResponse<SignUpResponse>>() {
                    })
                    .getResponseBody();

            StepVerifier.create(signUpResponse)
                    .expectNextMatches(response -> response.getPayload().getEmail().equals(expectedSignUpResponse.getEmail())
                            && response.getPayload().getRoles().equals(expectedSignUpResponse.getRoles()))
                    .thenCancel()
                    .verify();
        }

        final LoginRequest loginRequest = new LoginRequest(
                userEmail,
                userPassword
        );

        final StandardResponse<LoginResponse> loginResponse = webTestClient.post()
                .uri("/api/v1/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(loginRequest))
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<LoginResponse>>() {
                })
                .getResponseBody()
                .blockFirst();

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getPayload());
        assertEquals(loginResponse.getPayload().getEmail(), userEmail);
        assertEquals(loginResponse.getPayload().getRoles().size(), roles.size());
        assertEquals(loginResponse.getPayload().getTokenType(), "Bearer");

        return loginResponse.getPayload().getTokenType() + " " + loginResponse.getPayload().getAccessToken();
    }
}
