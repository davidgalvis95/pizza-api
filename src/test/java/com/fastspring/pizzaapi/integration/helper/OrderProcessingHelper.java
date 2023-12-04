package com.fastspring.pizzaapi.integration.helper;

import com.fastspring.pizzaapi.dto.order.OrderRequest;
import com.fastspring.pizzaapi.dto.order.OrderResponse;
import com.fastspring.pizzaapi.dto.order.PizzaOrderRequest;
import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.dto.product.Addition;
import com.fastspring.pizzaapi.dto.product.Cheese;
import com.fastspring.pizzaapi.dto.product.Pizza;
import com.fastspring.pizzaapi.dto.product.PizzaBase;
import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.enums.ProductType;

import java.util.List;
import java.util.UUID;

public class OrderProcessingHelper {

    private static final UUID neapolitanBaseId = UUID.fromString("0104adaf-10da-472c-8661-a84991be9caf");

    private static final UUID sicilianBaseId = UUID.fromString("47f85d00-98c4-4a08-b4c6-eeb8bc301a91");

    private static final UUID mozzarellaCheeseId = UUID.fromString("be89e36d-5701-477c-9158-91e9ce0d8768");

    private static final UUID parmesanCheeseId = UUID.fromString("41227ebf-e387-471d-a5c1-a6a74d335312");

    public static final UUID beefId = UUID.fromString("46166f8c-8d3f-46b7-858b-5ffdea957669");

    private static final UUID peperoniId = UUID.fromString("845f4e12-1471-4087-8f63-181730df1de6");

    private static final UUID promoCode = UUID.fromString("11872936-8d27-4ec8-9c6e-229223eeb7ea");

    private static final UUID nonExistentProductId = UUID.fromString("51872936-8d27-4ec8-9c6e-229223eeb8ea");

    private static final String neapolitanName = "Neapolitan";

    private static final String mozzarellaCheeseName = "Mozzarella";

    private static final String beefName = "Beef";

    private static final String peperoniName = "Peperoni";

    public static OrderResponse generateOrderResponse(){
        return OrderResponse.builder()
                .promoCode(promoCode)
                .promoCodeDescription("30 percent off in the total purchase price")
                .priceWithPromotion(68)
                .priceWithoutPromotion(97)
                .pizzas(List.of(
                        Pizza.builder()
                                .base(PizzaBase.builder()
                                        .id(neapolitanBaseId)
                                        .name(neapolitanName)
                                        .build())
                                .cheese(Cheese.builder()
                                        .id(mozzarellaCheeseId)
                                        .name(mozzarellaCheeseName)
                                        .build())
                                .additions(List.of(
                                        Addition.builder()
                                                .id(peperoniId)
                                                .name(peperoniName)
                                                .amount(3)
                                                .build(),
                                        Addition.builder()
                                                .id(beefId)
                                                .name(beefName)
                                                .amount(2)
                                                .build()
                                ))
                                .size(PizzaSize.BIG)
                                .unitPrice(41)
                                .build(),
                        Pizza.builder()
                                .base(PizzaBase.builder()
                                        .id(neapolitanBaseId)
                                        .name(neapolitanName)
                                        .build())
                                .cheese(Cheese.builder()
                                        .id(mozzarellaCheeseId)
                                        .name(mozzarellaCheeseName)
                                        .build())
                                .additions(List.of(
                                        Addition.builder()
                                                .id(peperoniId)
                                                .name(peperoniName)
                                                .amount(3)
                                                .build(),
                                        Addition.builder()
                                                .id(beefId)
                                                .name(beefName)
                                                .amount(2)
                                                .build()
                                ))
                                .size(PizzaSize.MEDIUM)
                                .unitPrice(32)
                                .build(),
                        Pizza.builder()
                                .base(PizzaBase.builder()
                                        .id(neapolitanBaseId)
                                        .name(neapolitanName)
                                        .build())
                                .cheese(Cheese.builder()
                                        .id(mozzarellaCheeseId)
                                        .name(mozzarellaCheeseName)
                                        .build())
                                .additions(List.of(
                                        Addition.builder()
                                                .id(peperoniId)
                                                .name(peperoniName)
                                                .amount(3)
                                                .build(),
                                        Addition.builder()
                                                .id(beefId)
                                                .name(beefName)
                                                .amount(2)
                                                .build()
                                ))
                                .size(PizzaSize.SMALL)
                                .unitPrice(24)
                                .build()
                ))
                .build();
    }

    public static OrderRequest generateOrderRequest(){
        return OrderRequest.builder()
                .pizzaRequests(List.of(
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(mozzarellaCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(3)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(beefId)
                                                .quantity(2)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.BIG)
                                .build(),
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(mozzarellaCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(3)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(beefId)
                                                .quantity(2)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.MEDIUM)
                                .build(),
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(mozzarellaCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(3)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(beefId)
                                                .quantity(2)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.SMALL)
                                .build()
                ))
                .promoCode(promoCode)
                .build();
    }

    public static OrderRequest generateBadOrderRequestWithExtraBase(){
        return OrderRequest.builder()
                .pizzaRequests(List.of(
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(sicilianBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(parmesanCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(3)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(beefId)
                                                .quantity(2)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.BIG)
                                .build()
                ))
                .promoCode(promoCode)
                .build();
    }

    public static OrderRequest generateBadOrderRequestWithExtraCheese(){
        return OrderRequest.builder()
                .pizzaRequests(List.of(
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(parmesanCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(3)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(beefId)
                                                .quantity(2)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.BIG)
                                .build()
                ))
                .promoCode(promoCode)
                .build();
    }

    public static OrderRequest generateBadOrderRequestWithAdditionQGreaterThan3(){
        return OrderRequest.builder()
                .pizzaRequests(List.of(
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(mozzarellaCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(4)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(beefId)
                                                .quantity(2)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.BIG)
                                .build()
                ))
                .promoCode(promoCode)
                .build();
    }

    public static OrderRequest generateBadOrderRequestWithRepeatedAdditions(){
        return OrderRequest.builder()
                .pizzaRequests(List.of(
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(mozzarellaCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(3)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(2)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.BIG)
                                .build()
                ))
                .promoCode(promoCode)
                .build();
    }

    public static OrderRequest generateBadOrderRequestWithProductIdNotMatchingItsType(){
        return OrderRequest.builder()
                .pizzaRequests(List.of(
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(neapolitanBaseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(mozzarellaCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(3)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.BIG)
                                .build()
                ))
                .promoCode(promoCode)
                .build();
    }

    public static OrderRequest generateBadOrderRequestWithNotExistentProduct(){
        return OrderRequest.builder()
                .pizzaRequests(List.of(
                        PizzaOrderRequest.builder()
                                .products(List.of(
                                        ProductOrderDto.builder()
                                                .productType(ProductType.BASE)
                                                .id(nonExistentProductId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.CHEESE)
                                                .id(mozzarellaCheeseId)
                                                .quantity(1)
                                                .build(),
                                        ProductOrderDto.builder()
                                                .productType(ProductType.ADDITION)
                                                .id(peperoniId)
                                                .quantity(3)
                                                .build()
                                ))
                                .pizzaSize(PizzaSize.BIG)
                                .build()
                ))
                .promoCode(promoCode)
                .build();
    }
}
