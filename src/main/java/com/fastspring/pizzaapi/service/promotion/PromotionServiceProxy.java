package com.fastspring.pizzaapi.service.promotion;

import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.model.enums.DescriptiveCode;
import com.fastspring.pizzaapi.model.Promotion;
import com.fastspring.pizzaapi.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Primary
@RequiredArgsConstructor
public class PromotionServiceProxy implements PromotionService {

    private final List<PromotionService> promotionServices;

    private final PromotionRepository promotionRepository;

    @Override
    public Mono<OrderResponse.OrderResponseBuilder> applyPromotionAndRecalculatePrice(
            final OrderResponse.OrderResponseBuilder orderResponseBuilder,
            final UUID promoCode,
            final Optional<Promotion> promotion) {
        if (promoCode != null) {
            return getImportantService(promoCode).flatMap(response -> response.entrySet().stream()
                    .map((entry) -> entry.getValue()
                            .applyPromotionAndRecalculatePrice(orderResponseBuilder, promoCode, Optional.of(entry.getKey())))
                    .findFirst()
                    .orElse(Mono.just(orderResponseBuilder)));
        }
        return Mono.just(orderResponseBuilder);
    }

    private Mono<Map<Promotion, PromotionService>> getImportantService(UUID promoCode) {
        return promotionRepository.findById(promoCode)
                .flatMap(promoCodeInfo -> {
                    if (promoCodeInfo.getActive()) {
                        return Mono.just(Map.of(promoCodeInfo, matchServiceByCodeDescription(promoCodeInfo.getDescriptiveCode(), promoCode)));
                    }
                    return Mono.error(new IllegalArgumentException("Current promo code " + promoCode + "is expired"));
                });
    }

    private PromotionService matchServiceByCodeDescription(final DescriptiveCode descriptiveCode, final UUID promoCode) {

        return switch (descriptiveCode) {
            case C_50_OFF, C_30_OFF -> matchService(PercentOffPromotionService.class);
            case C_10_USD_OFF_PURCHASE_GRATER_THAN_30 -> matchService(PurchaseGreaterThanDiscountService.class);
            case C_2_X_1 -> matchService(TwoXOnePromotionService.class);
            default -> throw new IllegalArgumentException("No service instance of " + PromotionService.class + " matches code: " + promoCode);
        };
    }

    private PromotionService matchService(final Class<?> promotionServiceClass) {
        return promotionServices.stream()
                .filter(promotionServiceClass::isInstance)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No service instance of " + PromotionService.class + " matches required criteria"));
    }
}
