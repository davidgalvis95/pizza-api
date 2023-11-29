package com.fastspring.pizzaapi.repository;

import com.fastspring.pizzaapi.model.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, UUID> {
    Flux<Product> findProductByProductIdIn(List<UUID> productIds);
}
