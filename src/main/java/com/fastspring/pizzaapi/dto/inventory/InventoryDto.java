package com.fastspring.pizzaapi.dto.inventory;

import com.fastspring.pizzaapi.model.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryDto {

    private UUID productId;

    private String productName;

    private Integer availableQuantity;

    private ProductType type;
}
