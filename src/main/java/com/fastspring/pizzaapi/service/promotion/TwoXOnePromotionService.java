package com.fastspring.pizzaapi.service.promotion;

import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.dto.product.Pizza;
import com.fastspring.pizzaapi.model.Promotion;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TwoXOnePromotionService implements PromotionService{
    @Override
    public Mono<OrderResponse.OrderResponseBuilder> applyPromotionAndRecalculatePrice(
            final OrderResponse.OrderResponseBuilder orderResponseBuilder,
            final UUID promoCode,
            final Optional<Promotion> promotion) {

        final List<Pizza> pizzas = orderResponseBuilder.build().getPizzas();

        if (promotion.isPresent()) {
            List<Pizza> pizzasWithPrice;
            int pizzasWithPriceSize;
            if(pizzas.size() % 2 == 0) {
                //adds the half more expensive ones
                pizzasWithPriceSize = pizzas.size() / 2;
                pizzasWithPrice = pizzas.subList(0, pizzasWithPriceSize);
            }else {
                //adds the half more expensive ones
                pizzasWithPriceSize = (pizzas.size() -1) / 2;
                //adds the remaining one which do not have any pair to be counted
                pizzasWithPrice = pizzas.subList(0, pizzasWithPriceSize);
                pizzasWithPrice.add(pizzas.get(pizzas.size() -1));
            }
            //calculates price for those added
            final int newPriceWithPromotion = pizzasWithPrice.stream()
                    .map(Pizza::getUnitPrice)
                    .reduce(0, Integer::sum);
            orderResponseBuilder.priceWithPromotion(newPriceWithPromotion);
        }
        return Mono.just(orderResponseBuilder);
    }
}
