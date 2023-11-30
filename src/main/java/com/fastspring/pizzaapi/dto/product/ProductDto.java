package com.fastspring.pizzaapi.dto.product;

import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.enums.ProductType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    @NotNull
    private ProductType type;

    @NotNull
    private String productName;

    @NotNull
    @NotEmpty
    private Map<@Valid PizzaSize, Integer> priceBySize;

    @NotNull
    private Integer initialInventory;
}
