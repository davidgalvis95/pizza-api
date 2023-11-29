package com.fastspring.pizzaapi.dto.order;

import com.fastspring.pizzaapi.model.enums.PizzaSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PizzaOrderRequest {
    private List<ProductOrderDto> products;
    private PizzaSize pizzaSize;
}
