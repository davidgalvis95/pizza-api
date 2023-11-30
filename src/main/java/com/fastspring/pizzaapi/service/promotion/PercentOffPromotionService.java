package com.fastspring.pizzaapi.service.promotion;

import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.model.Promotion;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PercentOffPromotionService implements PromotionService {

    @Override
    public Mono<OrderResponse.OrderResponseBuilder> applyPromotionAndRecalculatePrice(
            final OrderResponse.OrderResponseBuilder orderResponseBuilder,
            final UUID promoCode,
            final Optional<Promotion> promotionOptional) {
        if (promotionOptional.isPresent()) {
            final Promotion promotion = promotionOptional.get();
            final String descriptiveCode = promotion.getDescriptiveCode().name();
            final Pattern pattern = Pattern.compile("\\d+");
            final Matcher matcher = pattern.matcher(descriptiveCode);

            if (matcher.find()) {
                final String number = matcher.group();
                final int percentOff = Integer.parseInt(number);
                final int priceWithoutPromotion = orderResponseBuilder.build().getPriceWithoutPromotion();
                final int priceWithPromotion = priceWithoutPromotion - (priceWithoutPromotion * percentOff/100);
                orderResponseBuilder.priceWithPromotion(priceWithPromotion);
                orderResponseBuilder.promoCode(promoCode);
                orderResponseBuilder.promoCodeDescription(promotion.getDescription());
            }
        }
        return Mono.just(orderResponseBuilder);
    }
}
