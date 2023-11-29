package com.fastspring.pizzaapi.service.order;

import com.fastspring.pizzaapi.dto.order.OrderRequest;
import com.fastspring.pizzaapi.dto.order.OrderResponse;
import reactor.core.publisher.Mono;

public interface OrderService {
    Mono<OrderResponse> processOrder(final OrderRequest orderRequest);
}
