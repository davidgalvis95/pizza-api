package com.fastspring.pizzaapi.controller;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.model.Promotion;
import com.fastspring.pizzaapi.service.promotion.PromotionManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping
public class PromotionController {

    private final PromotionManagementService promotionManagementService;

    public PromotionController(PromotionManagementService promotionManagementService) {
        this.promotionManagementService = promotionManagementService;
    }


    @PutMapping
    public Mono<ResponseEntity<StandardResponse<Promotion>>> deactivatePromotion(@RequestParam UUID promotionCode) {
        return promotionManagementService.deactivatePromotion(promotionCode)
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<Promotion>builder()
                                .payload(response)
                                .message("Promotion with id: " + response.getCode() + " has been deactivated")
                                .build()
                        ));
    }

    @PutMapping
    public Mono<ResponseEntity<StandardResponse<Promotion>>> activatePromotion(@RequestParam UUID promotionCode) {
        return promotionManagementService.activatePromotion(promotionCode)
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<Promotion>builder()
                                .payload(response)
                                .message("Promotion with id: " + response.getCode() + " has been activated")
                                .build()
                        ));
    }
}
