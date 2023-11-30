package com.fastspring.pizzaapi.dto.order;

import com.fastspring.pizzaapi.dto.product.Pizza;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private List<Pizza> pizzas;
    private Integer priceWithoutPromotion;
    private Integer priceWithPromotion;
    private UUID promoCode;
    private String promoCodeDescription;
}
