package com.fastspring.pizzaapi.dto.product;

import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.enums.ProductType;
import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ProductDto {

    private ProductType type;
    private String productName;
    private Map<PizzaSize, Integer> priceBySize;
    private Integer initialInventory;

    public ProductDto() {};
}
