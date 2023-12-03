package com.fastspring.pizzaapi.integration;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.auth.LoginRequest;
import com.fastspring.pizzaapi.dto.auth.LoginResponse;
import com.fastspring.pizzaapi.dto.auth.SignUpResponse;
import com.fastspring.pizzaapi.dto.auth.SignupRequest;
import com.fastspring.pizzaapi.dto.inventory.InventoryDto;
import com.fastspring.pizzaapi.dto.inventory.InventoryRequest;
import com.fastspring.pizzaapi.dto.inventory.InventoryResponse;
import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.model.User;
import com.fastspring.pizzaapi.model.enums.ProductType;
import com.fastspring.pizzaapi.model.enums.Role;
import com.fastspring.pizzaapi.repository.UserRepository;
import org.junit.jupiter.api.*;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class InventoryTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    private static String managerAccessToken;

    private static String userAccessToken;

    private final AuthenticationHelper authenticationHelper = new AuthenticationHelper();

    @BeforeEach
    void setUp() {
        final String userEmail1 = "user.manager@test.com";
        final String userPassword1 = "password123";
        final Set<Role> userRoles1 = Set.of(Role.MANAGER, Role.USER);
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
    @Order(1)
    void getInventoriesForProductsTest() {
        final String productId1 = "47f85d00-98c4-4a08-b4c6-eeb8bc301a91";
        final String productId2 = "46166f8c-8d3f-46b7-858b-5ffdea957669";

        final InventoryResponse expectedInventoryResponse = InventoryResponse.builder()
                .inventories(Arrays.asList(
                        InventoryDto.builder()
                                .availableQuantity(100)
                                .productId(UUID.fromString(productId1))
                                .type(ProductType.BASE)
                                .productName("Sicilian")
                                .build(),
                        InventoryDto.builder()
                                .availableQuantity(100)
                                .productId(UUID.fromString(productId2))
                                .type(ProductType.ADDITION)
                                .productName("Beef")
                                .build()))
                .build();

        final Flux<StandardResponse<InventoryResponse>> inventoryResponse = webTestClient.get()
                .uri("/api/v1/inventory/query?productIds=" + productId1 + "," + productId2)
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<InventoryResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(inventoryResponse)
                .expectNextMatches(response -> {
                    final boolean payloadCheck = response.getPayload() != null;
                    final List<InventoryDto> inventoryDtoList = response.getPayload().getInventories();

                    final InventoryDto product1 = inventoryDtoList.stream()
                            .filter(i -> i.getProductId().equals(UUID.fromString(productId1)))
                            .findFirst()
                            .orElse(null);

                    final InventoryDto product2 = inventoryDtoList.stream()
                            .filter(i -> i.getProductId().equals(UUID.fromString(productId2)))
                            .findFirst()
                            .orElse(null);

                    final boolean product1Check = product1 != null &&
                            product1.getType().equals(expectedInventoryResponse.getInventories().get(0).getType()) &&
                            product1.getProductId().equals(expectedInventoryResponse.getInventories().get(0).getProductId()) &&
                            product1.getProductName().equals(expectedInventoryResponse.getInventories().get(0).getProductName()) &&
                            product1.getAvailableQuantity().equals(expectedInventoryResponse.getInventories().get(0).getAvailableQuantity());

                    final boolean product2Check = product2 != null &&
                            product2.getType().equals(expectedInventoryResponse.getInventories().get(1).getType()) &&
                            product2.getProductId().equals(expectedInventoryResponse.getInventories().get(1).getProductId()) &&
                            product2.getProductName().equals(expectedInventoryResponse.getInventories().get(1).getProductName()) &&
                            product2.getAvailableQuantity().equals(expectedInventoryResponse.getInventories().get(1).getAvailableQuantity());

                    return payloadCheck && product1Check && product2Check;
                })
                .thenCancel()
                .verify();
    }

    @Test
    @Order(2)
    void getInventoriesForProductsWhenProductDoNotExistTest() {
        final String productId1 = "97f85d00-98c4-4a08-b4c6-eeb8bc301a91";
        final String productId2 = "46166f8c-8d3f-46b7-858b-5ffdea957669";

        final InventoryResponse expectedInventoryResponse = InventoryResponse.builder()
                .inventories(List.of(
                        InventoryDto.builder()
                                .availableQuantity(100)
                                .productId(UUID.fromString(productId2))
                                .type(ProductType.ADDITION)
                                .productName("Beef")
                                .build()))
                .build();

        final Flux<StandardResponse<InventoryResponse>> inventoryResponse = webTestClient.get()
                .uri("/api/v1/inventory/query?productIds=" + productId1 + "," + productId2)
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<InventoryResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(inventoryResponse)
                .expectNextMatches(response -> {
                    final boolean payloadCheck = response.getPayload() != null;
                    final List<InventoryDto> inventoryDtoList = response.getPayload().getInventories();

                    final InventoryDto product1 = inventoryDtoList.stream()
                            .filter(i -> i.getProductId().equals(UUID.fromString(productId2)))
                            .findFirst()
                            .orElse(null);

                    final InventoryDto product2 = inventoryDtoList.stream()
                            .filter(i -> i.getProductId().equals(UUID.fromString(productId1)))
                            .findFirst()
                            .orElse(null);

                    final boolean product1Check = product1 != null &&
                            product1.getType().equals(expectedInventoryResponse.getInventories().get(0).getType()) &&
                            product1.getProductId().equals(expectedInventoryResponse.getInventories().get(0).getProductId()) &&
                            product1.getProductName().equals(expectedInventoryResponse.getInventories().get(0).getProductName()) &&
                            product1.getAvailableQuantity().equals(expectedInventoryResponse.getInventories().get(0).getAvailableQuantity());

                    final boolean product2Check = product2 == null;
                    return payloadCheck && product1Check && product2Check;
                })
                .thenCancel()
                .verify();
    }

    @Test
    @Order(3)
    void getInventoriesForProductsWhenForbiddenTest() {
        final String productId1 = "97f85d00-98c4-4a08-b4c6-eeb8bc301a91";
        final String productId2 = "46166f8c-8d3f-46b7-858b-5ffdea957669";

        final Flux<StandardResponse<InventoryResponse>> inventoryResponse = webTestClient.get()
                .uri("/api/v1/inventory/query?productIds=" + productId1 + "," + productId2)
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<InventoryResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(inventoryResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(4)
    void getInventoriesForProductsWhenUnauthorizedTest() {
        final String productId1 = "97f85d00-98c4-4a08-b4c6-eeb8bc301a91";
        final String productId2 = "46166f8c-8d3f-46b7-858b-5ffdea957669";

        final Flux<StandardResponse<InventoryResponse>> inventoryResponse = webTestClient.get()
                .uri("/api/v1/inventory/query?productIds=" + productId1 + "," + productId2)
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<InventoryResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(inventoryResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(5)
    void refillInventoryForProductTest() {
        final String productId1 = "47f85d00-98c4-4a08-b4c6-eeb8bc301a91";
        final String productId2 = "46166f8c-8d3f-46b7-858b-5ffdea957669";

        final InventoryRequest inventoryRequest = InventoryRequest.builder()
                .products(List.of(
                        ProductOrderDto.builder()
                                .id(UUID.fromString(productId1))
                                .quantity(10)
                                .productType(ProductType.BASE)
                                .build(),
                        ProductOrderDto.builder()
                                .id(UUID.fromString(productId2))
                                .quantity(15)
                                .productType(ProductType.ADDITION)
                                .build()
                ))
                .build();

        final InventoryResponse expectedInventoryResponse = InventoryResponse.builder()
                .inventories(Arrays.asList(
                        InventoryDto.builder()
                                .availableQuantity(110)
                                .productId(UUID.fromString(productId1))
                                .type(ProductType.BASE)
                                .productName("Sicilian")
                                .build(),
                        InventoryDto.builder()
                                .availableQuantity(115)
                                .productId(UUID.fromString(productId2))
                                .type(ProductType.ADDITION)
                                .productName("Beef")
                                .build()))
                .build();

        final Flux<StandardResponse<InventoryResponse>> inventoryResponse = webTestClient.post()
                .uri("/api/v1/inventory/refill")
                .body(BodyInserters.fromValue(inventoryRequest))
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<InventoryResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(inventoryResponse)
                .expectNextMatches(response -> {
                    final boolean payloadCheck = response.getPayload() != null;
                    final List<InventoryDto> inventoryDtoList = response.getPayload().getInventories();

                    final InventoryDto product1 = inventoryDtoList.stream()
                            .filter(i -> i.getProductId().equals(UUID.fromString(productId1)))
                            .findFirst()
                            .orElse(null);

                    final InventoryDto product2 = inventoryDtoList.stream()
                            .filter(i -> i.getProductId().equals(UUID.fromString(productId2)))
                            .findFirst()
                            .orElse(null);

                    final boolean product1Check = product1 != null &&
                            product1.getType().equals(expectedInventoryResponse.getInventories().get(0).getType()) &&
                            product1.getProductId().equals(expectedInventoryResponse.getInventories().get(0).getProductId()) &&
                            product1.getProductName().equals(expectedInventoryResponse.getInventories().get(0).getProductName()) &&
                            product1.getAvailableQuantity().equals(expectedInventoryResponse.getInventories().get(0).getAvailableQuantity());

                    final boolean product2Check = product2 != null &&
                            product2.getType().equals(expectedInventoryResponse.getInventories().get(1).getType()) &&
                            product2.getProductId().equals(expectedInventoryResponse.getInventories().get(1).getProductId()) &&
                            product2.getProductName().equals(expectedInventoryResponse.getInventories().get(1).getProductName()) &&
                            product2.getAvailableQuantity().equals(expectedInventoryResponse.getInventories().get(1).getAvailableQuantity());

                    return payloadCheck && product1Check && product2Check;
                })
                .thenCancel()
                .verify();
    }

    @Test
    @Order(6)
    void refillInventoryForProductWhenOneOfThemDoNotExistTest() {
        final String productId1 = "47f85d00-98c4-4a08-b4c6-eeb8bc301a91";
        final String productId2 = "86166f8c-8d3f-46b7-858b-5ffdea957669";

        final InventoryRequest inventoryRequest = InventoryRequest.builder()
                .products(List.of(
                        ProductOrderDto.builder()
                                .id(UUID.fromString(productId1))
                                .quantity(10)
                                .productType(ProductType.BASE)
                                .build(),
                        ProductOrderDto.builder()
                                .id(UUID.fromString(productId2))
                                .quantity(15)
                                .productType(ProductType.ADDITION)
                                .build()
                ))
                .build();

        final InventoryResponse expectedInventoryResponse = InventoryResponse.builder()
                .inventories(List.of(
                        InventoryDto.builder()
                                .availableQuantity(120)
                                .productId(UUID.fromString(productId1))
                                .type(ProductType.BASE)
                                .productName("Sicilian")
                                .build()))
                .build();

        final Flux<StandardResponse<InventoryResponse>> inventoryResponse = webTestClient.post()
                .uri("/api/v1/inventory/refill")
                .body(BodyInserters.fromValue(inventoryRequest))
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<InventoryResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(inventoryResponse)
                .expectNextMatches(response -> {
                    final boolean payloadCheck = response.getPayload() != null;
                    final List<InventoryDto> inventoryDtoList = response.getPayload().getInventories();

                    final InventoryDto product1 = inventoryDtoList.stream()
                            .filter(i -> i.getProductId().equals(UUID.fromString(productId1)))
                            .findFirst()
                            .orElse(null);

                    final InventoryDto product2 = inventoryDtoList.stream()
                            .filter(i -> i.getProductId().equals(UUID.fromString(productId2)))
                            .findFirst()
                            .orElse(null);

                    final boolean product1Check = product1 != null &&
                            product1.getType().equals(expectedInventoryResponse.getInventories().get(0).getType()) &&
                            product1.getProductId().equals(expectedInventoryResponse.getInventories().get(0).getProductId()) &&
                            product1.getProductName().equals(expectedInventoryResponse.getInventories().get(0).getProductName()) &&
                            product1.getAvailableQuantity().equals(expectedInventoryResponse.getInventories().get(0).getAvailableQuantity());

                    final boolean product2Check = product2 == null;

                    return payloadCheck && product1Check && product2Check;
                })
                .thenCancel()
                .verify();
    }

    @Test
    @Order(7)
    void refillInventoryForProductWhenForbiddenTest() {
        final String productId1 = "47f85d00-98c4-4a08-b4c6-eeb8bc301a91";
        final String productId2 = "46166f8c-8d3f-46b7-858b-5ffdea957669";

        final InventoryRequest inventoryRequest = InventoryRequest.builder()
                .products(List.of(
                        ProductOrderDto.builder()
                                .id(UUID.fromString(productId1))
                                .quantity(10)
                                .productType(ProductType.BASE)
                                .build(),
                        ProductOrderDto.builder()
                                .id(UUID.fromString(productId2))
                                .quantity(15)
                                .productType(ProductType.ADDITION)
                                .build()
                ))
                .build();

        final Flux<StandardResponse<InventoryResponse>> inventoryResponse = webTestClient.post()
                .uri("/api/v1/inventory/refill")
                .body(BodyInserters.fromValue(inventoryRequest))
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<InventoryResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(inventoryResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(8)
    void refillInventoryForProductWhenUnauthorizedTest() {
        final String productId1 = "47f85d00-98c4-4a08-b4c6-eeb8bc301a91";
        final String productId2 = "46166f8c-8d3f-46b7-858b-5ffdea957669";

        final InventoryRequest inventoryRequest = InventoryRequest.builder()
                .products(List.of(
                        ProductOrderDto.builder()
                                .id(UUID.fromString(productId1))
                                .quantity(10)
                                .productType(ProductType.BASE)
                                .build(),
                        ProductOrderDto.builder()
                                .id(UUID.fromString(productId2))
                                .quantity(15)
                                .productType(ProductType.ADDITION)
                                .build()
                ))
                .build();

        final Flux<StandardResponse<InventoryResponse>> inventoryResponse = webTestClient.post()
                .uri("/api/v1/inventory/refill")
                .body(BodyInserters.fromValue(inventoryRequest))
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<InventoryResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(inventoryResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }
}
