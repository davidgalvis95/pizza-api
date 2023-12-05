package com.fastspring.pizzaapi.integration;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.order.OrderRequest;
import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.dto.order.OrderResponseWrapperDto;
import com.fastspring.pizzaapi.dto.product.Pizza;
import com.fastspring.pizzaapi.integration.helper.AuthenticationHelper;
import com.fastspring.pizzaapi.integration.helper.OrderProcessingHelper;
import com.fastspring.pizzaapi.model.Inventory;
import com.fastspring.pizzaapi.model.User;
import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.enums.Role;
import com.fastspring.pizzaapi.repository.InventoryRepository;
import com.fastspring.pizzaapi.repository.OrderRepository;
import com.fastspring.pizzaapi.repository.UserRepository;
import com.fastspring.pizzaapi.service.auth.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OrderProcessingTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtService jwtService;

    private static String managerAccessToken;

    private static String userAccessToken;

    private final AuthenticationHelper authenticationHelper = new AuthenticationHelper();

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.4")
            .withExposedPorts(27017);


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
    @Order(9)
    void processOrderTest() {
        final OrderRequest orderRequest = OrderProcessingHelper.generateOrderRequest();
        final OrderResponse expectedOrderResponse = OrderProcessingHelper.generateOrderResponse();
        final String email = jwtService.extractUsername(userAccessToken.substring(7));
        final User loggedUser = userRepository.findByEmail(email).block();
        assertNotNull(loggedUser);

        final Flux<StandardResponse<OrderResponse>> orderProcessingResponse = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest))
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(orderProcessingResponse)
                .expectNextMatches(response -> {
                    final OrderResponse payload = response.getPayload();
                    final Integer priceWithoutPromotion = payload.getPriceWithoutPromotion();
                    final Integer priceWithPromotion = payload.getPriceWithPromotion();
                    final UUID promoCode = payload.getPromoCode();
                    final String promoCodeDescription = payload.getPromoCodeDescription();

                    final boolean orderCheck = priceWithoutPromotion.equals(expectedOrderResponse.getPriceWithoutPromotion()) &&
                            priceWithPromotion.equals(expectedOrderResponse.getPriceWithPromotion()) &&
                            promoCode.equals(expectedOrderResponse.getPromoCode()) &&
                            promoCodeDescription.equals(expectedOrderResponse.getPromoCodeDescription());

                    final Pizza bigPizza = payload.getPizzas().stream()
                            .filter(pizza -> pizza.getSize().equals(PizzaSize.BIG))
                            .findFirst()
                            .orElse(null);

                    final Pizza mediumPizza = payload.getPizzas().stream()
                            .filter(pizza -> pizza.getSize().equals(PizzaSize.BIG))
                            .findFirst()
                            .orElse(null);

                    final Pizza smallPizza = payload.getPizzas().stream()
                            .filter(pizza -> pizza.getSize().equals(PizzaSize.BIG))
                            .findFirst()
                            .orElse(null);

                    final Pizza expectedBigPizza = expectedOrderResponse.getPizzas().stream()
                            .filter(pizza -> pizza.getSize().equals(PizzaSize.BIG))
                            .findFirst()
                            .orElse(null);

                    final Pizza expectedMediumPizza = expectedOrderResponse.getPizzas().stream()
                            .filter(pizza -> pizza.getSize().equals(PizzaSize.BIG))
                            .findFirst()
                            .orElse(null);

                    final Pizza expectedSmallPizza = expectedOrderResponse.getPizzas().stream()
                            .filter(pizza -> pizza.getSize().equals(PizzaSize.BIG))
                            .findFirst()
                            .orElse(null);

                    final boolean checkSmallPizza = smallPizza != null &&
                            smallPizza.getSize().equals(expectedSmallPizza.getSize()) &&
                            smallPizza.getAdditions().equals(expectedSmallPizza.getAdditions()) &&
                            smallPizza.getCheese().equals(expectedSmallPizza.getCheese()) &&
                            smallPizza.getBase().equals(expectedSmallPizza.getBase()) &&
                            smallPizza.getUnitPrice().equals(expectedSmallPizza.getUnitPrice());

                    final boolean checkMediumPizza = mediumPizza != null &&
                            mediumPizza.getSize().equals(expectedMediumPizza.getSize()) &&
                            mediumPizza.getAdditions().equals(expectedMediumPizza.getAdditions()) &&
                            mediumPizza.getCheese().equals(expectedMediumPizza.getCheese()) &&
                            mediumPizza.getBase().equals(expectedMediumPizza.getBase()) &&
                            mediumPizza.getUnitPrice().equals(expectedMediumPizza.getUnitPrice());

                    final boolean checkBigPizza = bigPizza != null &&
                            bigPizza.getSize().equals(expectedBigPizza.getSize()) &&
                            bigPizza.getAdditions().equals(expectedBigPizza.getAdditions()) &&
                            bigPizza.getCheese().equals(expectedBigPizza.getCheese()) &&
                            bigPizza.getBase().equals(expectedBigPizza.getBase()) &&
                            bigPizza.getUnitPrice().equals(expectedBigPizza.getUnitPrice());

                    return Objects.nonNull(payload.getOrderId()) && orderCheck && checkSmallPizza && checkMediumPizza && checkBigPizza;
                })
                .thenCancel()
                .verify();

        final Mono<List<com.fastspring.pizzaapi.model.Order>> orders = orderRepository.findAll().collectList();

        StepVerifier.create(orders)
                .expectNextMatches(response -> {
                    final com.fastspring.pizzaapi.model.Order order = response.get(0);
                    return response.size() == 1 && Objects.nonNull(order.getOrderId()) &&
                            order.getUserId().equals(loggedUser.getId()) &&
                            order.getPriceWithoutPromotion().equals(expectedOrderResponse.getPriceWithoutPromotion()) &&
                            order.getPriceWithPromotion().equals(expectedOrderResponse.getPriceWithPromotion()) &&
                            order.getPromoCodeDescription().equals(expectedOrderResponse.getPromoCodeDescription()) &&
                            order.getPromoCode().equals(expectedOrderResponse.getPromoCode());
                })
                .thenCancel()
                .verify();

        final List<com.fastspring.pizzaapi.model.Order> orderList = orders.block();

        final Flux<StandardResponse<OrderResponseWrapperDto>> getOrdersResponse = webTestClient.get()
                .uri("/api/v1/order/all?userId="+loggedUser.getId())
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponseWrapperDto>>() {
                })
                .getResponseBody();


        StepVerifier.create(getOrdersResponse)
                .expectNextMatches(response -> {
                    final List<OrderResponse> orderResponseList = response.getPayload().getOrders();
                    final OrderResponse payload = response.getPayload().getOrders().get(0);
                    final Integer priceWithoutPromotion = payload.getPriceWithoutPromotion();
                    final Integer priceWithPromotion = payload.getPriceWithPromotion();
                    final UUID promoCode = payload.getPromoCode();
                    final String promoCodeDescription = payload.getPromoCodeDescription();
                    final com.fastspring.pizzaapi.model.Order expectedFirstOrder = Objects.requireNonNull(orderList).get(0);

                    return orderResponseList.size() == 1 &&
                            payload.getOrderId().equals(expectedFirstOrder.getOrderId()) &&
                            priceWithoutPromotion.equals(expectedFirstOrder.getPriceWithoutPromotion()) &&
                            priceWithPromotion.equals(expectedFirstOrder.getPriceWithPromotion()) &&
                            promoCode.equals(expectedFirstOrder.getPromoCode()) &&
                            promoCodeDescription.equals(expectedFirstOrder.getPromoCodeDescription());
                })
                .thenCancel()
                .verify();

        final Flux<StandardResponse<OrderResponseWrapperDto>> getAllOrdersResponse = webTestClient.get()
                .uri("/api/v1/order/all")
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponseWrapperDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllOrdersResponse)
                .expectNextMatches(response -> response.getPayload().getOrders().size() == 1 &&
                        response.getPayload().getOrders().get(0).getOrderId().equals(Objects.requireNonNull(orderList).get(0).getOrderId()))
                .thenCancel()
                .verify();

        final Flux<StandardResponse<OrderResponseWrapperDto>> getAllOrdersForUser = webTestClient.get()
                .uri("/api/v1/order/user/all")
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponseWrapperDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllOrdersForUser)
                .expectNextMatches(response -> response.getPayload().getOrders().size() == 1 &&
                        response.getPayload().getOrders().get(0).getOrderId().equals(Objects.requireNonNull(orderList).get(0).getOrderId()))
                .thenCancel()
                .verify();
    }

    @Test
    @Order(10)
    void getAllOrdersWhenUnauthorizedTest() {
        final Flux<StandardResponse<OrderResponse>> getAllOrdersResponse = webTestClient.get()
                .uri("/api/v1/order/all")
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllOrdersResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(11)
    void getAllOrdersWhenForbiddenTest() {
        final Flux<StandardResponse<OrderResponse>> getAllOrdersResponse = webTestClient.get()
                .uri("/api/v1/order/all")
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllOrdersResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(12)
    void getAllOrdersForUserWhenUnauthorizedTest() {
        final Flux<StandardResponse<OrderResponseWrapperDto>> getAllOrdersForUser = webTestClient.get()
                .uri("/api/v1/order/user/all")
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponseWrapperDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllOrdersForUser)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(13)
    void getAllOrdersForUserWhenForbiddenTest() {
        final Flux<StandardResponse<OrderResponseWrapperDto>> getAllOrdersForUser = webTestClient.get()
                .uri("/api/v1/order/user/all")
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponseWrapperDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllOrdersForUser)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(14)
    void sendBadOrderRequestsTest() {
        final String expectedErrorMessage = "Invalid pizza request: Cannot send more than one addition with same id, " +
                "its minimum amount is 1 maximum amount is 3, for base and cheese amount allowed is only 1, " +
                "and can ask only for one of its type";

        final OrderRequest orderRequest1 = OrderProcessingHelper.generateBadOrderRequestWithExtraBase();

        final Flux<StandardResponse<OrderResponse>> badOrderProcessingResponse1 = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest1))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(badOrderProcessingResponse1)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().equals(expectedErrorMessage))
                .thenCancel()
                .verify();

        final OrderRequest orderRequest2 = OrderProcessingHelper.generateBadOrderRequestWithExtraCheese();

        final Flux<StandardResponse<OrderResponse>> badOrderProcessingResponse2 = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest2))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(badOrderProcessingResponse2)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().equals(expectedErrorMessage))
                .thenCancel()
                .verify();

        final OrderRequest orderRequest3 = OrderProcessingHelper.generateBadOrderRequestWithRepeatedAdditions();

        final Flux<StandardResponse<OrderResponse>> badOrderProcessingResponse3 = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest3))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(badOrderProcessingResponse3)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().equals(expectedErrorMessage))
                .thenCancel()
                .verify();

        final OrderRequest orderRequest4 = OrderProcessingHelper.generateBadOrderRequestWithAdditionQGreaterThan3();

        final Flux<StandardResponse<OrderResponse>> badOrderProcessingResponse4 = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest4))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(badOrderProcessingResponse4)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().equals(expectedErrorMessage))
                .thenCancel()
                .verify();

        final OrderRequest orderRequest5 = OrderProcessingHelper.generateBadOrderRequestWithProductIdNotMatchingItsType();

        final Flux<StandardResponse<OrderResponse>> badOrderProcessingResponse5 = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest5))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(badOrderProcessingResponse5)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().contains("Product type")
                        && response.getError().contains("does not exist for product id"))
                .thenCancel()
                .verify();

        final OrderRequest orderRequest6 = OrderProcessingHelper.generateBadOrderRequestWithNotExistentProduct();

        final Flux<StandardResponse<OrderResponse>> badOrderProcessingResponse6 = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest6))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(badOrderProcessingResponse6)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().equals("Product not found: 51872936-8d27-4ec8-9c6e-229223eeb8ea"))
                .thenCancel()
                .verify();
    }

    @Test
    @Order(15)
    void processOrderTestWhenInactivePromoCode() {
        final OrderRequest orderRequest = OrderProcessingHelper.generateOrderRequest();
        final UUID inactivePromoCode = UUID.fromString("b7611773-ae6b-482a-9f03-427712793d32");
        orderRequest.setPromoCode(inactivePromoCode);

        final Flux<StandardResponse<OrderResponse>> orderProcessingResponse = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(orderProcessingResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().equals("Current promo code " + inactivePromoCode + " is expired"))
                .thenCancel()
                .verify();
    }

    @Test
    @Order(16)
    void processOrderTestWhenNonexistentPromoCode() {
        final OrderRequest orderRequest = OrderProcessingHelper.generateOrderRequest();
        final UUID inactivePromoCode = UUID.fromString("c7611773-ae6b-482a-9f03-427712793d32");
        orderRequest.setPromoCode(inactivePromoCode);

        final Flux<StandardResponse<OrderResponse>> orderProcessingResponse = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(orderProcessingResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().equals("No promo code found for id: " + inactivePromoCode))
                .thenCancel()
                .verify();
    }

    @Test
    @Order(17)
    void processOrderWhenNoAvailableInventoryForProduct() {
        inventoryRepository.findInventoryByProductId(OrderProcessingHelper.beefId)
                .flatMap(i -> inventoryRepository.save(Inventory.builder()
                        .id(i.getId())
                        .newRecord(false)
                        .productId(OrderProcessingHelper.beefId)
                        .availableQuantity(5)
                        .build()))
                .block();

        final OrderRequest orderRequest6 = OrderProcessingHelper.generateOrderRequest();

        final Flux<StandardResponse<OrderResponse>> badOrderProcessingResponse6 = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(orderRequest6))
                .exchange()
                .expectStatus().is5xxServerError()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(badOrderProcessingResponse6)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null
                        && response.getError().equals("Insufficient inventory for product: " + OrderProcessingHelper.beefId))
                .thenCancel()
                .verify();

        final Mono<Inventory> updatedInventory = inventoryRepository.findInventoryByProductId(OrderProcessingHelper.beefId)
                .flatMap(i -> inventoryRepository.save(Inventory.builder()
                        .id(i.getId())
                        .newRecord(false)
                        .productId(OrderProcessingHelper.beefId)
                        .availableQuantity(100)
                        .build()));

        StepVerifier.create(updatedInventory)
                .expectNextMatches(response -> response.getAvailableQuantity() == 100 && response.getProductId().equals(OrderProcessingHelper.beefId))
                .thenCancel()
                .verify();
    }

    @Test
    @Order(18)
    void processOrderWhenUnauthorizedTest() {
        final OrderRequest orderRequest = OrderProcessingHelper.generateOrderRequest();
        final UUID inactivePromoCode = UUID.fromString("c7611773-ae6b-482a-9f03-427712793d32");
        orderRequest.setPromoCode(inactivePromoCode);

        final Flux<StandardResponse<OrderResponse>> orderProcessingResponse = webTestClient.post()
                .uri("/api/v1/order/place")
                .body(BodyInserters.fromValue(orderRequest))
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(orderProcessingResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(19)
    void processOrderWhenForbiddenTest() {
        final OrderRequest orderRequest = OrderProcessingHelper.generateOrderRequest();
        final UUID inactivePromoCode = UUID.fromString("c7611773-ae6b-482a-9f03-427712793d32");
        orderRequest.setPromoCode(inactivePromoCode);

        final Flux<StandardResponse<OrderResponse>> orderProcessingResponse = webTestClient.post()
                .uri("/api/v1/order/place")
                .header("Authorization", managerAccessToken)
                .body(BodyInserters.fromValue(orderRequest))
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<OrderResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(orderProcessingResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }
}
