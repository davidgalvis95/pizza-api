package com.fastspring.pizzaapi.model;

import com.fastspring.pizzaapi.dto.product.Pizza;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private UUID orderId;

    private UUID userId;

    private List<Pizza> pizzas;

    private Integer priceWithoutPromotion;

    private Integer priceWithPromotion;

    private UUID promoCode;

    private String promoCodeDescription;
}
