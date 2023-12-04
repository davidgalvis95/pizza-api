package com.fastspring.pizzaapi.integration;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.product.ProductDto;
import com.fastspring.pizzaapi.dto.product.ProductResponseDto;
import com.fastspring.pizzaapi.dto.product.ProductsResponse;
import com.fastspring.pizzaapi.model.Product;
import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.enums.ProductType;
import com.fastspring.pizzaapi.model.enums.Role;
import com.fastspring.pizzaapi.repository.ProductRepository;
import com.fastspring.pizzaapi.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProductManagementTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

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
    @Order(18)
    void getAllProductsTest() {
        final Flux<StandardResponse<ProductsResponse>> getAllProductsResponse = webTestClient.get()
                .uri("/api/v1/product/all")
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductsResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllProductsResponse)
                .expectNextMatches(response -> {
                    final ProductsResponse payload = response.getPayload();
                    final boolean productsPriceCheck = payload.getProducts().stream()
                            .allMatch(p -> p.getInventory() == 100);
                    final List<ProductResponseDto> cheeseProds = payload.getProducts().stream()
                            .filter(p -> p.getType().equals(ProductType.CHEESE))
                            .toList();
                    final List<ProductResponseDto> additions = payload.getProducts().stream()
                            .filter(p -> p.getType().equals(ProductType.ADDITION))
                            .toList();
                    final List<ProductResponseDto> baseProds = payload.getProducts().stream()
                            .filter(p -> p.getType().equals(ProductType.BASE))
                            .toList();

                    boolean productTypesCheck = baseProds.size() == 4 && additions.size() == 9 && cheeseProds.size() == 4;
                    return payload.getProducts().size() == 17 && productTypesCheck && productsPriceCheck;
                })
                .thenCancel()
                .verify();
    }

    @Test
    @Order(19)
    void getAllProductsWhenForbiddenTest() {
        final Flux<StandardResponse<ProductsResponse>> getAllProductsResponse = webTestClient.get()
                .uri("/api/v1/product/all")
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductsResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllProductsResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(20)
    void getAllProductsWhenUnauthorizedTest() {
        final Flux<StandardResponse<ProductsResponse>> getAllProductsResponse = webTestClient.get()
                .uri("/api/v1/product/all")
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductsResponse>>() {
                })
                .getResponseBody();

        StepVerifier.create(getAllProductsResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(21)
    void addNewProductTest(){
        final ProductDto productDto = new ProductDto(
                ProductType.ADDITION,
                "Garlic",
                Map.of(PizzaSize.BIG, 4, PizzaSize.MEDIUM, 3, PizzaSize.SMALL, 2),
                100
        );

        final Flux<StandardResponse<ProductResponseDto>> addNewProductResponse = webTestClient.post()
                .uri("/api/v1/product/new")
                .header("Authorization", managerAccessToken)
                .body(BodyInserters.fromValue(productDto))
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductResponseDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(addNewProductResponse)
                .expectNextMatches(response -> {
                    final ProductResponseDto newProduct = response.getPayload();
                    return newProduct.getProductName().equals(productDto.getProductName()) &&
                            newProduct.getInventory().equals(productDto.getInitialInventory()) &&
                            newProduct.getPriceBySize().get(PizzaSize.BIG).equals(productDto.getPriceBySize().get(PizzaSize.BIG)) &&
                            newProduct.getPriceBySize().get(PizzaSize.MEDIUM).equals(productDto.getPriceBySize().get(PizzaSize.MEDIUM)) &&
                            newProduct.getPriceBySize().get(PizzaSize.SMALL).equals(productDto.getPriceBySize().get(PizzaSize.SMALL)) &&
                            newProduct.getType().equals(productDto.getType());
                })
                .thenCancel()
                .verify();
    }

    @Test
    @Order(22)
    void addNewProductWhenForbiddenTest(){
        final ProductDto productDto = new ProductDto(
                ProductType.ADDITION,
                "Garlic",
                Map.of(PizzaSize.BIG, 4, PizzaSize.MEDIUM, 3, PizzaSize.SMALL, 2),
                100
        );

        final Flux<StandardResponse<ProductResponseDto>> addNewProductResponse = webTestClient.post()
                .uri("/api/v1/product/new")
                .header("Authorization", userAccessToken)
                .body(BodyInserters.fromValue(productDto))
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductResponseDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(addNewProductResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(23)
    void addNewProductWhenUnauthorizedTest(){
        final ProductDto productDto = new ProductDto(
                ProductType.ADDITION,
                "Garlic",
                Map.of(PizzaSize.BIG, 4, PizzaSize.MEDIUM, 3, PizzaSize.SMALL, 2),
                100
        );

        final Flux<StandardResponse<ProductResponseDto>> addNewProductResponse = webTestClient.post()
                .uri("/api/v1/product/new")
                .body(BodyInserters.fromValue(productDto))
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductResponseDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(addNewProductResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(24)
    void addNewProductWhenNoPricesForAllSizesTest(){
        final ProductDto productDto = new ProductDto(
                ProductType.ADDITION,
                "Garlic",
                Map.of(PizzaSize.BIG, 4, PizzaSize.MEDIUM, 3),
                100
        );

        final Flux<StandardResponse<ProductResponseDto>> addNewProductResponse = webTestClient.post()
                .uri("/api/v1/product/new")
                .header("Authorization", managerAccessToken)
                .body(BodyInserters.fromValue(productDto))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductResponseDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(addNewProductResponse)
                .expectNextMatches(response ->
                    response.getError().equals("Product type ADDITION must include price for BIG, MEDIUM ans SMALL sizes")
                )
                .thenCancel()
                .verify();
    }

    @Test
    @Order(25)
    void addNewProductWhenBaseHasNoProperPriceAssignment(){
        final ProductDto productDto = new ProductDto(
                ProductType.BASE,
                "FakeBase",
                Map.of(PizzaSize.BIG, 10),
                100
        );

        final Flux<StandardResponse<ProductResponseDto>> addNewProductResponse = webTestClient.post()
                .uri("/api/v1/product/new")
                .header("Authorization", managerAccessToken)
                .body(BodyInserters.fromValue(productDto))
                .exchange()
                .expectStatus().isBadRequest()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductResponseDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(addNewProductResponse)
                .expectNextMatches(response ->
                        response.getError().equals("Product type BASE must only have price for NOT_APPLICABLE pizza size")
                )
                .thenCancel()
                .verify();
    }

    @Test
    @Order(26)
    void deleteProductByIdTest(){

        final UUID chiliId = UUID.fromString("9076e375-1c6f-49fd-b019-e0f7ce348be3");

        final Flux<StandardResponse<ProductResponseDto>> deleteProductResponse = webTestClient.delete()
                .uri("/api/v1/product/remove?productId=" + chiliId)
                .header("Authorization", managerAccessToken)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductResponseDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(deleteProductResponse)
                .expectNextMatches(response -> response.getMessage().equals("Product with id: " + chiliId + " has been deleted from our records")
                )
                .thenCancel()
                .verify();

        final Product queryProductResponse = productRepository.findById(chiliId).block();

        assertNull(queryProductResponse);
    }

    @Test
    @Order(27)
    void deleteProductByIdWhenForbiddenTest(){

        final UUID chiliId = UUID.fromString("9076e375-1c6f-49fd-b019-e0f7ce348be3");

        final Flux<StandardResponse<ProductResponseDto>> deleteProductResponse = webTestClient.delete()
                .uri("/api/v1/product/remove?productId=" + chiliId)
                .header("Authorization", userAccessToken)
                .exchange()
                .expectStatus().isForbidden()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductResponseDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(deleteProductResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }

    @Test
    @Order(28)
    void deleteProductByIdWhenUnauthorizedTest(){

        final UUID chiliId = UUID.fromString("9076e375-1c6f-49fd-b019-e0f7ce348be3");

        final Flux<StandardResponse<ProductResponseDto>> deleteProductResponse = webTestClient.delete()
                .uri("/api/v1/product/remove?productId=" + chiliId)
                .exchange()
                .expectStatus().isUnauthorized()
                .returnResult(new ParameterizedTypeReference<StandardResponse<ProductResponseDto>>() {
                })
                .getResponseBody();

        StepVerifier.create(deleteProductResponse)
                .expectNextMatches(response -> response.getPayload() == null && response.getMessage() == null)
                .thenCancel()
                .verify();
    }
}
