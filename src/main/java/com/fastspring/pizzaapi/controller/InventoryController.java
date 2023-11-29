package com.fastspring.pizzaapi.controller;

import com.fastspring.pizzaapi.dto.*;
import com.fastspring.pizzaapi.dto.inventory.InventoryRequest;
import com.fastspring.pizzaapi.dto.inventory.InventoryResponse;
import com.fastspring.pizzaapi.dto.inventory.InventoryDto;
import com.fastspring.pizzaapi.model.Inventory;
import com.fastspring.pizzaapi.service.inventory.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }


    @PostMapping("/refill")
    public Mono<ResponseEntity<StandardResponse<InventoryResponse>>> refillInventoriesFroProducts(@RequestBody InventoryRequest inventoryRefillRequest) {
        return inventoryService.updateProductsInventory(inventoryRefillRequest.getProducts(), true)
                .collectList()
                .map(result -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<InventoryResponse>builder()
                                .payload(mapResultToInventoryRefillResponse(result))
                                .build()
                        ));
    }

    @GetMapping("/query")
    public Mono<ResponseEntity<StandardResponse<InventoryResponse>>> getInventoryForProducts(@RequestParam List<UUID> productIds) {
        return inventoryService.getInventoryForProducts(productIds)
                .collectList()
                .map(result -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<InventoryResponse>builder()
                                .payload(mapResultToInventoryRefillResponse(result))
                                .build()
                        ));
    }

    private InventoryResponse mapResultToInventoryRefillResponse(List<Inventory> response) {
        return InventoryResponse.builder().inventories(
                response.stream()
                        .map(i -> InventoryDto.builder()
                                .productId(i.getProductId())
                                .productName(i.getProduct().getName())
                                .type(i.getProduct().getType())
                                .availableQuantity(i.getAvailableQuantity())
                                .build())
                        .toList()
        ).build();
    }
}
