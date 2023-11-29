package com.fastspring.pizzaapi.dto.order;

import com.fastspring.pizzaapi.model.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductProcessOrderDto {
    private UUID id;
    private Integer quantity;
    private Integer realQuantity;
    private ProductType productType;
}
