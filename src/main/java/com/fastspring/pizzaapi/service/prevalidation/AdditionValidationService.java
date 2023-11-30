package com.fastspring.pizzaapi.service.prevalidation;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.model.enums.ProductType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdditionValidationService extends OrderPreValidationService {

    @Override
    public boolean validateProducts(List<ProductOrderDto> products, ProductType productType) {
        final List<ProductOrderDto> additions =  super.getProductsByType(products, productType);
        if(!super.validateProducts(additions, productType)) {
            return false;
        }
        final List<UUID> additionIds = additions.stream().map(ProductOrderDto::getId).toList();
        final Set<UUID> uniqueAdditionsIds = new HashSet<>(additionIds);
        if(additionIds.size() != uniqueAdditionsIds.size()){
            return false;
        }else {
            for (ProductOrderDto addition: additions) {
                if(addition.getQuantity() > 3 || addition.getQuantity() == null || addition.getQuantity() < 1) {
                    return false;
                }
            }
        }
        return true;
    }
}
