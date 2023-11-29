package com.fastspring.pizzaapi.dto.product;

import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private UUID id;
    private ProductType type;
    private String productName;
    private Map<PizzaSize, Integer> priceBySize;
    private Integer initialInventory;
}
