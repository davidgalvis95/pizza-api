package com.fastspring.pizzaapi.service.inventory;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.model.Inventory;
import com.fastspring.pizzaapi.repository.InventoryRepository;
import com.fastspring.pizzaapi.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;

    private final ProductRepository productRepository;


    public InventoryServiceImpl(final InventoryRepository inventoryRepository,
                                final ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Flux<Inventory> getInventoryForProducts(final List<UUID> productIds) {
        return inventoryRepository.findInventoryByProductIdIn(productIds)
                .flatMap(this::getInventoryWithProduct);
    }

    @Override
    public Mono<Void> checkForProductsInventory(List<ProductOrderDto> products) {

        return inventoryRepository.findInventoryByProductIdIn(
                        products.stream()
                                .map(ProductOrderDto::getId)
                                .toList()
                )
                .collectList()
                .flatMap(inventories -> {
                    try {
                        validateProductsInCurrentInventories(products, inventories);
                    } catch (RuntimeException e) {
                        return Mono.error(e);
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Flux<Inventory> updateProductsInventory(final List<ProductOrderDto> products, final boolean isRefill) {

        final Flux<Inventory> inventories =  inventoryRepository.findInventoryByProductIdIn(
                        products.stream()
                                .map(ProductOrderDto::getId)
                                .toList()
                )
                .flatMap(inventory -> updateInventory(products, inventory, isRefill));

        if(isRefill) {
            return inventories.flatMap(this::getInventoryWithProduct);
        }else {
            return inventories;
        }
    }

    private Mono<Inventory> getInventoryWithProduct(final Inventory inventory) {
        if (inventory.getProductId() == null) {
            return Mono.just(inventory);
        }
        return productRepository.findById(inventory.getProductId())
                .map(product -> {
                    inventory.setProduct(product);
                    return inventory;
                });
    }

    private Mono<Inventory> updateInventory(List<ProductOrderDto> products, Inventory inventory, final boolean isRefill) {
        return products.stream()
                .filter(p -> p.getId().equals(inventory.getId()))
                .findFirst()
                .map(productDto -> {
                    int newAvailableQuantity = inventory.getAvailableQuantity() +
                            (isRefill ? productDto.getQuantity() : -productDto.getQuantity());
                    inventory.setAvailableQuantity(newAvailableQuantity);
                    return inventoryRepository.save(inventory);
                })
                .orElseThrow(() -> new IllegalArgumentException("No product provided that matches the product id: " + inventory.getProductId() + " in inventory"));
    }

    private void validateProductsInCurrentInventories(List<ProductOrderDto> products, List<Inventory> inventories) {
        products.forEach(product -> {
            final Inventory currentInventoryForProduct = inventories.stream()
                    .filter(i -> i.getProductId().equals(product.getId())).findFirst()
                    .orElseThrow(() -> new RuntimeException("Product " + product.getId() + " is not present in inventory"));
            final int requestedQuantity = product.getQuantity();
            if (currentInventoryForProduct.getAvailableQuantity() < requestedQuantity) {
                throw new RuntimeException("Insufficient inventory for product: " + product.getId());
            }
        });
    }
}
