package com.fastspring.pizzaapi.dto.inventory;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
public class InventoryRequest {
    private List<ProductOrderDto> products;
}
