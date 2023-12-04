package com.fastspring.pizzaapi.integration;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.promotion.PromotionResponse;
import com.fastspring.pizzaapi.integration.helper.AuthenticationHelper;
import com.fastspring.pizzaapi.model.Promotion;
import com.fastspring.pizzaapi.model.enums.DescriptiveCode;
import com.fastspring.pizzaapi.model.enums.Role;
import com.fastspring.pizzaapi.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PromotionManagementTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    private static String managerAccessToken;

    private static String userAccessToken;

    private final AuthenticationHelper authenticationHelper = new AuthenticationHelper();

    @BeforeEach
    void setUp() {
        final String userEmail1 = "manager@test.com";
        final String userPassword1 = "password123";
        final Set<Role> userRoles1 = Set.of(Role.MANAGER);
        managerAccessToken = authenticationHelper.registerIfNotExistsAndLoginUser(
                webTestClient,
                userRepository,
                userEmail1,
                userPassword1,
                userRoles1
        );

        final String userEmail2 = "user@test.com";
        final String userPassword2 = "password123";
        final Set<Role> userRoles2 = Set.of(Role.USER);
        userAccessToken = authenticationHelper.registerIfNotExistsAndLoginUser(
                webTestClient,
                userRepository,
                userEmail2,
                userPassword2,
                userRoles2
        );
    }

    @Test
    @Order(29)
    void getAllPromotionsTest() {
        final Flux<StandardResponse<PromotionResponse>> getAllPromotionsResponse = webTestClient.get()
                .uri("/api/v1/promotion/all")
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<PromotionResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllPromotionsResponse)
                .expectNextMatches(response -> {
                    final PromotionResponse payload = response.getPayload();

                    final List<DescriptiveCode> expectedCodes = List.of(DescriptiveCode.C_2_X_1, DescriptiveCode.C_30_OFF, DescriptiveCode.C_50_OFF, DescriptiveCode.C_10_USD_OFF_PURCHASE_GRATER_THAN_30);
                    final List<DescriptiveCode> promotionCodes = payload.getPromotions().stream()
                            .map(Promotion::getDescriptiveCode)
                            .toList();

                    final List<Promotion> activeCodes = payload.getPromotions().stream()
                            .filter(Promotion::getActive)
                            .toList();
                    return activeCodes.size() == 1 &&
                            activeCodes.get(0).getDescriptiveCode().equals(DescriptiveCode.C_30_OFF) &&
                            promotionCodes.containsAll(expectedCodes);
                })
                .thenCancel()
                .verify();

    }

    @Test
    @Order(29)
    void getAllPromotionsWhenUnauthorizedTest() {
        final Flux<StandardResponse<PromotionResponse>> getAllPromotionsResponse = webTestClient.get()
                .uri("/api/v1/promotion/all")
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<PromotionResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllPromotionsResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(29)
    void getAllPromotionsWhenForbiddenTest() {
        final Flux<StandardResponse<PromotionResponse>> getAllPromotionsResponse = webTestClient.get()
                .uri("/api/v1/promotion/all")
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<PromotionResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllPromotionsResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(29)
    void deactivatePromotionTest() {
        final UUID _30OffPromoCode = UUID.fromString("11872936-8d27-4ec8-9c6e-229223eeb7ea");
        final Flux<StandardResponse<Promotion>> deactivatePromotionResponse = webTestClient.put()
                .uri("/api/v1/promotion/deactivate?promotionCode=" + _30OffPromoCode)
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<Promotion>>() {
                })
                .getResponseBody();

        StepVerifier.create(deactivatePromotionResponse)
                .expectNextMatches(response -> !response.getPayload().getActive() &&
                        response.getPayload().getDescriptiveCode().equals(DescriptiveCode.C_30_OFF))
                .thenCancel()
                .verify();
    }

    @Test
    @Order(29)
    void deactivatePromotionWhenUnauthorizedTest() {
        final UUID _30OffPromoCode = UUID.fromString("11872936-8d27-4ec8-9c6e-229223eeb7ea");
        final Flux<StandardResponse<Promotion>> deactivatePromotionResponse = webTestClient.put()
                .uri("/api/v1/promotion/deactivate?promotionCode=" + _30OffPromoCode)
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<Promotion>>() {
                })
                .getResponseBody();

        StepVerifier.create(deactivatePromotionResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(29)
    void deactivatePromotionWhenForbiddenTest() {
        final UUID _30OffPromoCode = UUID.fromString("11872936-8d27-4ec8-9c6e-229223eeb7ea");
        final Flux<StandardResponse<Promotion>> deactivatePromotionResponse = webTestClient.put()
                .uri("/api/v1/promotion/deactivate?promotionCode=" + _30OffPromoCode)
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<Promotion>>() {
                })
                .getResponseBody();

        StepVerifier.create(deactivatePromotionResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(29)
    void activatePromotionTest() {
        final UUID _30OffPromoCode = UUID.fromString("11872936-8d27-4ec8-9c6e-229223eeb7ea");
        final Flux<StandardResponse<Promotion>> deactivatePromotionResponse = webTestClient.put()
                .uri("/api/v1/promotion/activate?promotionCode=" + _30OffPromoCode)
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<Promotion>>() {
                })
                .getResponseBody();

        StepVerifier.create(deactivatePromotionResponse)
                .expectNextMatches(response -> response.getPayload().getActive() &&
                        response.getPayload().getDescriptiveCode().equals(DescriptiveCode.C_30_OFF))
                .thenCancel()
                .verify();
    }

    @Test
    @Order(29)
    void activatePromotionWhenUnauthorizedTest() {
        final UUID _30OffPromoCode = UUID.fromString("11872936-8d27-4ec8-9c6e-229223eeb7ea");
        final Flux<StandardResponse<Promotion>> activatePromotionResponse = webTestClient.put()
                .uri("/api/v1/promotion/activate?promotionCode=" + _30OffPromoCode)
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<Promotion>>() {
                })
                .getResponseBody();

        StepVerifier.create(activatePromotionResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(29)
    void activatePromotionWhenForbiddenTest() {
        final UUID _30OffPromoCode = UUID.fromString("11872936-8d27-4ec8-9c6e-229223eeb7ea");
        final Flux<StandardResponse<Promotion>> activatePromotionResponse = webTestClient.put()
                .uri("/api/v1/promotion/activate?promotionCode=" + _30OffPromoCode)
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<Promotion>>() {
                })
                .getResponseBody();

        StepVerifier.create(activatePromotionResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }
}
