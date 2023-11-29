package com.fastspring.pizzaapi.service.promotion;

import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.model.Promotion;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface PromotionService {

    Mono<OrderResponse.OrderResponseBuilder> applyPromotionAndRecalculatePrice(
            OrderResponse.OrderResponseBuilder orderResponseBuilder,
            UUID promoCode,
            Optional<Promotion> promotion);
}
