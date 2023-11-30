package com.fastspring.pizzaapi.service.prevalidation;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.model.enums.ProductType;

import java.util.List;

public class NotAdditionValidationService extends OrderPreValidationService {

    @Override
    public boolean validateProducts(List<ProductOrderDto> products, ProductType productType) {
        if(!super.validateProducts(products, productType)){
            return false;
        }else {
            final List<ProductOrderDto> productsByType = super.getProductsByType(products, productType);
            if(productsByType.size() != 1){
                return false;
            }else {
                return productsByType.get(0).getQuantity() == 1;
            }
        }
    }
}
