package com.fastspring.pizzaapi.service.promotion;

import com.fastspring.pizzaapi.model.Promotion;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PromotionManagementService {

    Mono<Promotion> activatePromotion(final UUID promotionCode);

    Mono<Promotion> deactivatePromotion(final UUID promotionCode);

}
