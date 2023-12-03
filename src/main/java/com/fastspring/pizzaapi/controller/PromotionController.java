package com.fastspring.pizzaapi.controller;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.model.Promotion;
import com.fastspring.pizzaapi.service.promotion.PromotionManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/promotion")
public class PromotionController {

    //TODO add the get all promotions request

    private final PromotionManagementService promotionManagementService;

    public PromotionController(PromotionManagementService promotionManagementService) {
        this.promotionManagementService = promotionManagementService;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/deactivate")
    public Mono<ResponseEntity<StandardResponse<Promotion>>> deactivatePromotion(@RequestParam UUID promotionCode) {
        return promotionManagementService.deactivatePromotion(promotionCode)
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<Promotion>builder()
                                .payload(response)
                                .message("Promotion with id: " + response.getCode() + " has been deactivated")
                                .build()
                        ));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/activate")
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
