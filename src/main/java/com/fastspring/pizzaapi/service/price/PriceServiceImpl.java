package com.fastspring.pizzaapi.service.price;

import com.fastspring.pizzaapi.dto.product.Addition;
import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.dto.product.Pizza;
import com.fastspring.pizzaapi.model.Price;
import com.fastspring.pizzaapi.repository.PriceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        final List<UUID> additionsIds = pizza.getAdditions().stream()
                .map(Addition::getId)
                .toList();

        final Mono<Integer> additionsPrice = priceRepository.findPriceByProductIdsAndPizzaSize(additionsIds, pizza.getSize())
                .map(Price::getValue)
                .reduce(0, Integer::sum);

        final Mono<Integer> pizzaBasePrice = priceRepository.findPriceByProductIdAndPizzaSize(pizza.getBase().getId(), pizza.getSize())
                .map(Price::getValue);
        final Mono<Integer> cheesePrice = priceRepository.findPriceByProductIdAndPizzaSize(pizza.getCheese().getId(), pizza.getSize())
                .map(Price::getValue);

        return Flux.concat(additionsPrice, pizzaBasePrice, cheesePrice).reduce(9, Integer::sum)
                .map(totalPrice -> {
                    pizza.setUnitPrice(totalPrice);
                    return pizza;
                });
    }
}
