package com.fastspring.pizzaapi.repository;

import com.fastspring.pizzaapi.model.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, UUID> {

    Flux<Order> findAllByUserId(final UUID userId);

    Mono<Order> findOrderByOrderId(final UUID orderId);
}
