package com.fastspring.pizzaapi.service.price;

import com.fastspring.pizzaapi.dto.order.OrderResponse;
import reactor.core.publisher.Mono;

public interface PriceService {
    Mono<OrderResponse.OrderResponseBuilder> calculatePrice(OrderResponse.OrderResponseBuilder orderResponseBuilder);
}
