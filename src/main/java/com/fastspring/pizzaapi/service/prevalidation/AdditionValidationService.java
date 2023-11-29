package com.fastspring.pizzaapi.service.prevalidation;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.model.enums.ProductType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdditionValidationService extends OrderPreValidationService {

    @Override
    public boolean validateProducts(List<ProductOrderDto> products, ProductType productType) {
        final List<ProductOrderDto> additions =  super.getProductsByType(products, productType);
        if(!super.validateProducts(additions, productType)) {
            return false;
        }
        final Set<ProductOrderDto> uniqueAdditions = new HashSet<>(additions);
        if(additions.size() != uniqueAdditions.size()){
            return false;
        }else {
            for (ProductOrderDto addition: additions) {
                if(addition.getQuantity() > 3) {
                    return false;
                }
            }
        }
        return true;
    }
}
