package com.fastspring.pizzaapi.service.promotion;

import com.fastspring.pizzaapi.dto.promotion.PromotionResponse;
import com.fastspring.pizzaapi.model.Promotion;
import com.fastspring.pizzaapi.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class PromotionManagementServiceImpl implements PromotionManagementService {

    private final PromotionRepository promotionRepository;

    public PromotionManagementServiceImpl(final PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public Mono<Promotion> activatePromotion(UUID promotionCode) {
        return getPromotionById(promotionCode)
                .flatMap(currentPromotion -> {
                    currentPromotion.setActive(true);
                    currentPromotion.setNewRecord(false);
                    return promotionRepository.save(currentPromotion);
                });
    }

    @Override
    public Mono<Promotion> deactivatePromotion(UUID promotionCode) {
        return getPromotionById(promotionCode)
                .flatMap(currentPromotion -> {
                    currentPromotion.setActive(false);
                    currentPromotion.setNewRecord(false);
                    return promotionRepository.save(currentPromotion);
                });
    }

    @Override
    public Mono<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll()
                .collectList()
                .map(PromotionResponse::new);
    }

    private Mono<Promotion> getPromotionById(UUID promotionCode) {
        return promotionRepository.findById(promotionCode)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No promotion found for id: " + promotionCode)));
    }
}
