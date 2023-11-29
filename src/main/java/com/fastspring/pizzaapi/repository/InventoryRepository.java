package com.fastspring.pizzaapi.repository;

import com.fastspring.pizzaapi.model.Inventory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryRepository extends ReactiveCrudRepository<Inventory, UUID> {
    Mono<Inventory> findInventoryByProductId(UUID productId);

    Flux<Inventory> findInventoryByProductIdIn(List<UUID> productId);

    Mono<Void> deleteByProductId(UUID productId);
}
