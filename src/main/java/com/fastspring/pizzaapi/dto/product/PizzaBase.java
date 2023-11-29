package com.fastspring.pizzaapi.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class PizzaBase {
    private UUID id;
    private String name;
}
