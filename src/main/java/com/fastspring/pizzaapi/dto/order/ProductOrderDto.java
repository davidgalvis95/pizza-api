package com.fastspring.pizzaapi.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fastspring.pizzaapi.model.Product;
import com.fastspring.pizzaapi.model.enums.ProductType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderDto {

    @NotNull
    private UUID id;

    private Integer quantity;

    @JsonIgnore
    private Integer realQuantity;

    @NotNull
    private ProductType productType;
}
