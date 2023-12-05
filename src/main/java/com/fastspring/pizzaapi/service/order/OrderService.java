package com.fastspring.pizzaapi.service.order;

import com.fastspring.pizzaapi.dto.order.OrderRequest;
import com.fastspring.pizzaapi.dto.order.OrderResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderService {
    Mono<OrderResponse> processOrder(final OrderRequest orderRequest);

    Flux<OrderResponse> getOrdersForUser();

    Flux<OrderResponse> getOrders(final UUID orderId);
}
