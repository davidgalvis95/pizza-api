package com.fastspring.pizzaapi.integration;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.auth.SignUpResponse;
import com.fastspring.pizzaapi.dto.auth.SignupRequest;
import com.fastspring.pizzaapi.model.User;
import com.fastspring.pizzaapi.model.enums.Role;
import com.fastspring.pizzaapi.repository.UserRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuthTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Order(16)
    void testUserCreationAndAuthentication() {

        AuthenticationHelper authenticationHelper = new AuthenticationHelper();
        final String userEmail1 = "manager@test.com";
        final String userPassword1 = "password123";
        final Set<Role> userRoles1 = Set.of(Role.MANAGER, Role.USER);
        final String token1 = authenticationHelper.registerIfNotExistsAndLoginUser(
                webTestClient,
                userRepository,
                userEmail1,
                userPassword1,
                userRoles1
        );

        final String userEmail2 = "user@test.com";
        final String userPassword2 = "password123";
        final Set<Role> userRoles2 = Set.of(Role.USER);
        final String token2 = authenticationHelper.registerIfNotExistsAndLoginUser(
                webTestClient,
                userRepository,
                userEmail2,
                userPassword2,
                userRoles2
        );


        assertNotNull(token1);
        assertNotNull(token2);
    }

    @Test
    @Order(17)
    void testCannotCreateUserAsAdmin() {
        final String userEmail = "amin@test.com";
        final String userPassword = "password123";
        final Set<Role> roles = Set.of(Role.ADMIN);

        final SignupRequest signupRequest = new SignupRequest(
                userEmail,
                userPassword,
                roles
        );

        final Flux<StandardResponse<SignUpResponse>> signUpResponse = webTestClient.post()
                .uri("/api/v1/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(signupRequest))
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<SignUpResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(signUpResponse)
                .expectNextMatches(response -> response.getError()
                        .equals("Users cannot be created as ADMIN using this endpoint, create the user as USER and " +
                                "ask an already admin user to mark that user as ADMIN"))
                .thenCancel()
                .verify();

    }
}
