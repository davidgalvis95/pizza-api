package com.fastspring.pizzaapi.repository;

import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.Price;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface PriceRepository extends ReactiveCrudRepository<Price, UUID> {

    @Query("select p.id, p.product_id, p.value, p.pizza_size from price p " +
            "where p.product_id in (:productIds) and p.pizza_size = :pizzaSize")
    Flux<Price> findPriceByProductIdsAndPizzaSize(@Param("productIds") final Collection<UUID> productIds,
                                                  @Param("pizzaSize") final PizzaSize pizzaSize);

    Mono<Price> findPriceByProductIdAndPizzaSize(final UUID productId, final PizzaSize pizzaSize);

    Mono<Price> findPriceByProductId(final UUID productId);

    Mono<Void> deleteByProductId(final UUID productId);
}
