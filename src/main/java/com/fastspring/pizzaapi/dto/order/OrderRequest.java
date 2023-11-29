package com.fastspring.pizzaapi.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Builder
@Value
@AllArgsConstructor
public class OrderRequest {
    List<PizzaOrderRequest> pizzaRequests;
    UUID promoCode;
}
