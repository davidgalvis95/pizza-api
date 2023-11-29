package com.fastspring.pizzaapi.dto.product;

import com.fastspring.pizzaapi.model.enums.PizzaSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pizza {
    private Cheese cheese;
    private PizzaBase base;
    private List<Addition> additions;
    private Integer unitPrice;
    private PizzaSize size;
}
