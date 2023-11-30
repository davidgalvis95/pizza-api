package com.fastspring.pizzaapi.service.order;

import com.fastspring.pizzaapi.dto.order.OrderRequest;
import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.dto.order.PizzaOrderRequest;
import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.dto.product.*;
import com.fastspring.pizzaapi.model.Product;
import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.enums.ProductType;
import com.fastspring.pizzaapi.service.inventory.InventoryService;
import com.fastspring.pizzaapi.service.prevalidation.AdditionValidationService;
import com.fastspring.pizzaapi.service.prevalidation.NotAdditionValidationService;
import com.fastspring.pizzaapi.service.prevalidation.OrderPreValidationService;
import com.fastspring.pizzaapi.service.price.PriceService;
import com.fastspring.pizzaapi.service.product.ProductService;
import com.fastspring.pizzaapi.service.promotion.PromotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final InventoryService inventoryService;

    private final ProductService productService;

    private final PriceService priceService;

    private final PromotionService promotionService;

    private final Map<ProductType, OrderPreValidationService> validationServices;


    public OrderServiceImpl(final InventoryService inventoryService,
                            final ProductService productService,
                            final PriceService priceService,
                            final PromotionService promotionService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.priceService = priceService;
        this.promotionService = promotionService;
        this.validationServices = Map.of(
                ProductType.ADDITION, new AdditionValidationService(),
                ProductType.BASE, new NotAdditionValidationService(),
                ProductType.CHEESE, new NotAdditionValidationService()
        );
    }

    public Mono<OrderResponse> processOrder(final OrderRequest orderRequest) {
        final List<ProductOrderDto> products = pizzaRequestsToListOfAggregatedProducts(orderRequest.getPizzaRequests());

        return preValidatePizzaRequests(orderRequest.getPizzaRequests())
                .then(productService.checkForProductExistence(products))
                .then(inventoryService.checkForProductsInventory(products))
                .then(productService.getAllProductsMatchingIds(products)
                        .collectList()
                        .map(productsList -> initializeOrderResponse(orderRequest, productsList)))
                .flatMap(priceService::calculatePrice)
                .flatMap(order -> promotionService.applyPromotionAndRecalculatePrice(order, orderRequest.getPromoCode(), Optional.empty())
                        .map(OrderResponse.OrderResponseBuilder::build))
                .flatMap(order -> inventoryService.updateProductsInventory(products, false)
                        .collectList()
                        .map(response -> order));
    }

    private Mono<Void> preValidatePizzaRequests(final List<PizzaOrderRequest> pizzaOrderRequests) {
        final List<Mono<Void>> orderValidations = pizzaOrderRequests.stream()
                .map(pizzaReq -> preValidateOrderRequest(pizzaReq.getProducts()))
                .toList();

        return Mono.when(orderValidations);
    }

    private Mono<Void> preValidateOrderRequest(final List<ProductOrderDto> products) {
        final boolean additionsValid = validationServices.get(ProductType.ADDITION).validateProducts(products, ProductType.ADDITION);
        final boolean baseIsValid = validationServices.get(ProductType.BASE).validateProducts(products, ProductType.BASE);
        final boolean cheeseIsValid = validationServices.get(ProductType.CHEESE).validateProducts(products, ProductType.CHEESE);

        if (!additionsValid || !baseIsValid || !cheeseIsValid) {
            return Mono.error(new IllegalArgumentException("Invalid pizza request: Cannot send more than one addition with same id, its minimum amount is 1 maximum amount is 3, " +
                    "for base and cheese amount allowed is only 1, and can ask only for one of its type"));
        }
        return Mono.empty();
    }

    private OrderResponse.OrderResponseBuilder initializeOrderResponse(final OrderRequest orderRequest,
                                                                       final List<Product> productsList) {
        return OrderResponse.builder()
                .pizzas(pizzaRequestsIntoListOfPizzas(orderRequest.getPizzaRequests(), productsList));
    }


    private List<Pizza> pizzaRequestsIntoListOfPizzas(final List<PizzaOrderRequest> pizzaOrderRequests,
                                                      final List<Product> products) {
        return pizzaOrderRequests.stream()
                .map(pizzaOrderRequest -> pizzaRequestIntoPizza(pizzaOrderRequest, products))
                .toList();
    }

    private Pizza pizzaRequestIntoPizza(final PizzaOrderRequest pizzaOrderRequest, final List<Product> products) {
        return Pizza.builder()
                .base(pizzaOrderRequest.getProducts().stream()
                        .filter(p -> p.getProductType().equals(ProductType.BASE))
                        .map(p -> PizzaBase.builder()
                                .id(p.getId())
                                .name(productService.getProductName(products, p))
                                .build())
                        .findFirst()
                        .orElse(null))
                .cheese(pizzaOrderRequest.getProducts().stream()
                        .filter(p -> p.getProductType().equals(ProductType.CHEESE))
                        .map(p -> Cheese.builder()
                                .id(p.getId())
                                .name(productService.getProductName(products, p))
                                .build())
                        .findFirst()
                        .orElse(null))
                .additions(pizzaOrderRequest.getProducts().stream()
                        .filter(p -> p.getProductType().equals(ProductType.ADDITION))
                        .map(p -> Addition.builder()
                                .id(p.getId())
                                .amount(p.getQuantity())
                                .name(productService.getProductName(products, p))
                                .build())
                        .toList())
                .size(pizzaOrderRequest.getPizzaSize())
                .build();
    }

    private List<ProductOrderDto> pizzaRequestsToListOfAggregatedProducts(List<PizzaOrderRequest> pizzaOrderRequests) {
        final Map<UUID, ProductOrderDto> currentProducts = new HashMap<>();
        for (PizzaOrderRequest pizzaOrderRequest : pizzaOrderRequests) {
            for (ProductOrderDto product : pizzaOrderRequest.getProducts()) {
                currentProducts.computeIfPresent(product.getId(), (key, value) -> {
                    final Integer newQ = value.getQuantity() + product.getQuantity();
                    final Integer newRealQ =
                            calculateQuantityBasedOnPizzaSize(pizzaOrderRequest.getPizzaSize(), newQ, product.getProductType());
                    return ProductOrderDto.builder()
                            .id(value.getId())
                            .productType(value.getProductType())
                            .quantity(newQ)
                            .realQuantity(newRealQ)
                            .build();
                });
                currentProducts.computeIfAbsent(product.getId(), (key) -> {
                    final Integer newRealQ =
                            calculateQuantityBasedOnPizzaSize(pizzaOrderRequest.getPizzaSize(), product.getQuantity(), product.getProductType());
                    return ProductOrderDto.builder()
                            .id(key)
                            .productType(product.getProductType())
                            .quantity(product.getQuantity())
                            .realQuantity(newRealQ)
                            .build();
                });
            }
        }
        return currentProducts.values().stream().toList();
    }

    private Integer calculateQuantityBasedOnPizzaSize(PizzaSize pizzaSize, final Integer baseQ, final ProductType productType) {
        if(productType.equals(ProductType.BASE)) {
            pizzaSize = PizzaSize.NOT_APPLICABLE;
        }

        return switch (pizzaSize) {
            case SMALL, NOT_APPLICABLE -> baseQ;
            case MEDIUM -> baseQ * 2;
            case BIG -> baseQ * 3;
            default -> throw new IllegalStateException("Unexpected value for pizza size: " + pizzaSize);
        };
    }
}
