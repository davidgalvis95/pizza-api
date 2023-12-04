package com.fastspring.pizzaapi.service.price;

import com.fastspring.pizzaapi.dto.product.Addition;
import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.dto.product.Pizza;
import com.fastspring.pizzaapi.model.Price;
import com.fastspring.pizzaapi.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PriceServiceImpl implements PriceService {

    private final PriceRepository priceRepository;

    public PriceServiceImpl(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public Mono<OrderResponse.OrderResponseBuilder> calculatePrice(final OrderResponse.OrderResponseBuilder orderResponseBuilder) {
        final List<Pizza> pizzas = orderResponseBuilder.build().getPizzas();
        return Flux.concat(pizzas.stream()
                        .map(this::calculatePizzaUnitPrice)
                        .collect(Collectors.toList()))
                .collectList()
                .map(pizzasList -> sortPizzasAndBuildOrderResponse(orderResponseBuilder, pizzasList));
    }

    private OrderResponse.OrderResponseBuilder sortPizzasAndBuildOrderResponse(
            final OrderResponse.OrderResponseBuilder orderResponseBuilder,
            final List<Pizza> pizzasList) {
        final List<Pizza> sortedPizzas = pizzasList.stream()
                .sorted((pizza1, pizza2) -> Integer.compare(pizza2.getUnitPrice(), pizza1.getUnitPrice()))
                .toList();
        return orderResponseBuilder.pizzas(pizzasList)
                .priceWithoutPromotion(calculateTotalOrderPrice(sortedPizzas));
    }

    private Integer calculateTotalOrderPrice(final List<Pizza> pizzas) {
        return pizzas.stream()
                .map(Pizza::getUnitPrice)
                .reduce(0, Integer::sum);
    }

    private Mono<Pizza> calculatePizzaUnitPrice(final Pizza pizza) {

        final Map<UUID, Integer> additionQuantityMap = new HashMap<>();
        final List<UUID> additionsIds = pizza.getAdditions().stream()
                .map(addition -> {
                    additionQuantityMap.put(addition.getId(), addition.getAmount());
                    return addition.getId();
                })
                .toList();

        final Mono<Integer> additionsPrice = priceRepository.findPriceByProductIdsAndPizzaSize(additionsIds, pizza.getSize())
                .map(price -> price.getValue() * additionQuantityMap.get(price.getProductId()))
                .reduce(0, Integer::sum)
                .doOnNext(res -> log.info("Additions price total: " + res));

        final Mono<Integer> pizzaBasePrice = priceRepository.findPriceByProductId(pizza.getBase().getId())
                .collectList()
                .map(priceList -> priceList.get(0).getValue())
                .doOnNext(res -> log.info("Base price total: " + res));

        final Mono<Integer> cheesePrice = priceRepository.findPriceByProductIdAndPizzaSize(pizza.getCheese().getId(), pizza.getSize())
                .map(Price::getValue)
                .doOnNext(res -> log.info("Cheese price total: " + res));


        return Flux.concat(additionsPrice, pizzaBasePrice, cheesePrice)
                .reduce(0, Integer::sum)
                .map(totalPrice -> {
                    pizza.setUnitPrice(totalPrice);
                    return pizza;
                });
    }
}
