package com.fastspring.pizzaapi.repository;

import com.fastspring.pizzaapi.model.Promotion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PromotionRepository extends ReactiveCrudRepository<Promotion, UUID> {
}
