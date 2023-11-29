package com.fastspring.pizzaapi.dto.inventory;

import com.fastspring.pizzaapi.model.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryDto {
    UUID productId;
    String productName;
    Integer availableQuantity;
    ProductType type;
}
