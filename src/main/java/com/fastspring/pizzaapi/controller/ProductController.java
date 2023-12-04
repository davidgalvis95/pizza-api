package com.fastspring.pizzaapi.controller;

import com.fastspring.pizzaapi.dto.StandardResponse;
import com.fastspring.pizzaapi.dto.product.ProductDto;
import com.fastspring.pizzaapi.dto.product.ProductResponseDto;
import com.fastspring.pizzaapi.dto.product.ProductsResponse;
import com.fastspring.pizzaapi.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(final ProductService productService) {
        this.productService = productService;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/all")
    public Mono<ResponseEntity<StandardResponse<ProductsResponse>>> getAllProducts() {
        return productService.getProducts()
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<ProductsResponse>builder()
                                .payload(response)
                                .build()
                        ));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/new")
    public Mono<ResponseEntity<StandardResponse<ProductResponseDto>>> addNewProduct(@RequestBody ProductDto productDto) {
        return productService.addNewProduct(productDto)
                .map(response -> ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<ProductResponseDto>builder()
                                .payload(response)
                                .message("Product with id: " + response.getId() + " has been added to our records")
                                .build()
                        ));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("remove")
    public Mono<ResponseEntity<StandardResponse<ProductResponseDto>>> deleteProductById(@RequestParam UUID productId) {
        return productService.deleteProduct(productId)
                .then(Mono.just(ResponseEntity.status(HttpStatus.OK)
                        .body(StandardResponse.<ProductResponseDto>builder()
                                .message("Product with id: " + productId + " has been deleted from our records")
                                .build()
                        )));
    }
}
