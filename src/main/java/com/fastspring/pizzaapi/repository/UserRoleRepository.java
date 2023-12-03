package com.fastspring.pizzaapi.repository;

import com.fastspring.pizzaapi.model.UserRole;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, UUID> {
    Flux<UserRole> findUserRolesByUserId(final UUID userId);
}
