package com.fastspring.pizzaapi.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class Cheese {
    private UUID id;
    private String name;
}
