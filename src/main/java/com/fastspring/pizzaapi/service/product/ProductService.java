package com.fastspring.pizzaapi.service.product;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.dto.product.ProductDto;
import com.fastspring.pizzaapi.dto.product.ProductResponseDto;
import com.fastspring.pizzaapi.model.Product;
import com.fastspring.pizzaapi.model.enums.ProductType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    Mono<Product> getProduct(final UUID productId);

    Flux<Product> getAllProductsMatchingIds(final List<ProductOrderDto> product);

    Mono<Void> checkForProductExistence(final List<ProductOrderDto> productList);

    String getProductName(final List<Product> products, final ProductOrderDto productModel);

    Mono<ProductResponseDto> addNewProduct(final ProductDto productDto);

    Mono<Void> deleteProduct(final UUID productId);
}
