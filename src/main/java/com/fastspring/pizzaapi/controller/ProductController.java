package com.fastspring.pizzaapi.controller;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.product.ProductDto;
import com.fastspring.pizzaapi.dto.product.ProductResponseDto;
import com.fastspring.pizzaapi.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @PostMapping("/new")
    public Mono<ResponseEntity<StandardResponse<ProductResponseDto>>> addNewProduct(@RequestBody ProductDto productDto) {
        return productService.addNewProduct(productDto)
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<ProductResponseDto>builder()
                                .payload(response)
                                .message("Product with id: " +response.getId() + " has been added to our records")
                                .build()
                        ));
    }

    @DeleteMapping
    public Mono<ResponseEntity<StandardResponse<ProductResponseDto>>> deleteProductById(@RequestParam UUID productId) {
        return productService.deleteProduct(productId)
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(StandardResponse.<ProductResponseDto>builder()
                                .message("Product with id: " +productId+ " has been deleted from our records")
                                .build()
                        ));
    }
}
