package com.fastspring.pizzaapi.service.inventory;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.model.Inventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    Mono<Void> checkForProductsInventory(List<ProductOrderDto> products);

    Flux<Inventory> getInventoryForProducts(List<UUID> values);

    Flux<Inventory> updateProductsInventory(final List<ProductOrderDto> products,
                                            final boolean isRefill);
}
