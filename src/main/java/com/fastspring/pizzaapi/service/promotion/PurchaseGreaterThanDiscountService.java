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
public class PurchaseGreaterThanDiscountService implements PromotionService {

    @Override
    public Mono<OrderResponse.OrderResponseBuilder> applyPromotionAndRecalculatePrice(
            final OrderResponse.OrderResponseBuilder orderResponseBuilder,
            final UUID promoCode,
            final Optional<Promotion> promotion) {
        if (promotion.isPresent()) {
            final String descriptiveCode = promotion.get().getDescriptiveCode().name();
            final Pattern pattern = Pattern.compile("\\d+");
            final Matcher matcher = pattern.matcher(descriptiveCode);

            int count = 1;
            int amountOff = 0;
            int minPurchase = 0;
            while (matcher.find()) {
                if(count == 1) {
                    amountOff = Integer.parseInt(matcher.group());
                }else {
                    minPurchase = Integer.parseInt(matcher.group());
                }
                count++;
            }
            final int priceWithoutPromotion = orderResponseBuilder.build().getPriceWithoutPromotion();
            if(priceWithoutPromotion > minPurchase) {
                final int priceWithPromotion = priceWithoutPromotion - amountOff;
                orderResponseBuilder.priceWithPromotion(priceWithPromotion);
            }else {
                return Mono.error(new IllegalArgumentException("Purchase muct be greater than $" + minPurchase + "to apply the promotional code: " + promoCode));
            }
        }
        return Mono.just(orderResponseBuilder);
    }
}
