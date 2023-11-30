package com.fastspring.pizzaapi.dto.inventory;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @NotEmpty
    private List<@Valid ProductOrderDto> products;
}
