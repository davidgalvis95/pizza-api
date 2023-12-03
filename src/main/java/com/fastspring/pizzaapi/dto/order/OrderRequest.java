package com.fastspring.pizzaapi.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class OrderRequest {

    @NotEmpty
    @NotNull
    private List<@Valid PizzaOrderRequest> pizzaRequests;

    private UUID promoCode;
}
