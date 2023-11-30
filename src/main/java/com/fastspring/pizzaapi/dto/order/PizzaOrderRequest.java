package com.fastspring.pizzaapi.dto.order;

import com.fastspring.pizzaapi.model.enums.PizzaSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Valid
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PizzaOrderRequest {

    @NotNull
    @NotEmpty
    private List<@Valid ProductOrderDto> products;

    @NotNull
    private PizzaSize pizzaSize;
}
