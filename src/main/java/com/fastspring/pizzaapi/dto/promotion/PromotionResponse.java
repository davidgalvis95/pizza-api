package com.fastspring.pizzaapi.dto.promotion;

import com.fastspring.pizzaapi.model.Promotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse {
    private List<Promotion> promotions;
}
