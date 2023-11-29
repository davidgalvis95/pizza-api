package com.fastspring.pizzaapi.service.prevalidation;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.model.enums.ProductType;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor
public abstract class OrderPreValidationService {
    public List<ProductOrderDto> getProductsByType(List<ProductOrderDto> products, ProductType productType) {
        return products.stream()
                .filter(product -> product.getProductType().equals(productType))
                .toList();
    }

    public boolean validateProducts(List<ProductOrderDto> products, ProductType productType) {
        return this.getProductsByType(products, productType).stream()
                .filter(p -> Objects.isNull(p.getProductType()))
                .toList()
                .isEmpty();
    }
}
