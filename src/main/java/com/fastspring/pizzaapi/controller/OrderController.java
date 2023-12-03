package com.fastspring.pizzaapi.controller;


import com.fastspring.pizzaapi.dto.order.OrderRequest;
import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.service.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/place")
    public Mono<ResponseEntity<StandardResponse<OrderResponse>>> processOrderRequest(@RequestBody OrderRequest orderRequest) {
        return orderService.processOrder(orderRequest)
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<OrderResponse>builder()
                                .payload(response)
                                .build()
                        ));
    }
}
