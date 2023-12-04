package com.fastspring.pizzaapi.service.product;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.dto.product.ProductDto;
import com.fastspring.pizzaapi.dto.product.ProductResponseDto;
import com.fastspring.pizzaapi.dto.product.ProductsResponse;
import com.fastspring.pizzaapi.model.Inventory;
import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.Price;
import com.fastspring.pizzaapi.model.Product;
import com.fastspring.pizzaapi.model.enums.ProductType;
import com.fastspring.pizzaapi.repository.InventoryRepository;
import com.fastspring.pizzaapi.repository.PriceRepository;
import com.fastspring.pizzaapi.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final InventoryRepository inventoryRepository;

    private final PriceRepository priceRepository;


    public ProductServiceImpl(final ProductRepository productRepository,
                              final InventoryRepository inventoryRepository,
                              final PriceRepository priceRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.priceRepository = priceRepository;
    }

    @Override
    public Mono<Product> getProduct(final UUID productId) {
        return productRepository.findById(productId);
    }

    @Override
    public Mono<Void> checkForProductExistence(final List<ProductOrderDto> productList) {
        final List<Mono<Product>> productMonos = productList.stream()
                .map(productDto -> getProduct(productDto.getId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Product not found: " + productDto.getId())))
                        .flatMap(product -> checkIfProductTypeMatchesForProductId(productDto, product)))
                .collect(Collectors.toList());

        return Mono.when(productMonos);
    }

    @Override
    public Flux<Product> getAllProductsMatchingIds(final List<ProductOrderDto> product) {
        return productRepository.findProductByProductIdIn(product.stream().map(ProductOrderDto::getId).toList());
    }

    @Override
    public String getProductName(final List<Product> products, final ProductOrderDto productModel) {
        return products.stream().filter(p1 -> p1.getProductId().equals(productModel.getId())).findFirst().map(Product::getName).orElse("");
    }

    @Override
    @Transactional
    public Mono<ProductResponseDto> addNewProduct(final ProductDto productDto) {

        final UUID newProductId = UUID.randomUUID();
        final UUID newInventoryId = UUID.randomUUID();

        if(productDto.getType().equals(ProductType.CHEESE) || productDto.getType().equals(ProductType.ADDITION)) {
            final Set<PizzaSize> targetSet = Set.of(PizzaSize.BIG, PizzaSize.MEDIUM, PizzaSize.SMALL);
            if(!new HashSet<>(productDto.getPriceBySize().keySet()).equals(targetSet)) {
                return Mono.error(new IllegalArgumentException("Product type " + productDto.getType() + " must include price for BIG, MEDIUM ans SMALL sizes"));
            };
        }else if(productDto.getType().equals(ProductType.BASE))  {
            if(productDto.getPriceBySize().size() == 1 || productDto.getPriceBySize().get(PizzaSize.NOT_APPLICABLE) == null){
                return Mono.error(new IllegalArgumentException("Product type BASE must only have price for NOT_APPLICABLE pizza size"));
            }
        }

        return saveProductAndReturn(productDto, newProductId)
                .flatMap(product -> saveInventoryForProductAndReturnBuilder(productDto, newProductId, newInventoryId, product))
                .flatMap(productResponse -> savePriceAndReturnResponse(productDto, newProductId, productResponse));
    }

    @Override
    public Mono<Void> deleteProduct(final UUID productId) {
        return inventoryRepository.deleteByProductId(productId)
                .then(priceRepository.deleteByProductId(productId))
                .then(productRepository.deleteById(productId));
    }

    @Override
    public Mono<ProductsResponse> getProducts() {
        return productRepository.findAll()
                .flatMap(product -> inventoryRepository.findInventoryByProductId(product.getId())
                        .map(inventory -> ProductResponseDto.builder()
                                .id(product.getId())
                                .type(product.getType())
                                .inventory(inventory.getAvailableQuantity()))
                        .flatMap(this::generateProductsBuilderFromPrice)
                )
                .collectList()
                .map(productsList -> ProductsResponse.builder()
                        .products(productsList)
                        .build());
    }

    private Mono<ProductResponseDto> generateProductsBuilderFromPrice(
            final ProductResponseDto.ProductResponseDtoBuilder productBuilder) {
        return priceRepository.findPriceByProductId(productBuilder.build().getId())
                .collectList()
                .map(prices -> {
                    final Map<PizzaSize, Integer> pricesMap = new HashMap<>();
                    prices.forEach(price -> pricesMap.put(price.getPizzaSize(), price.getValue()));
                    return pricesMap;
                })
                .map(priceMap -> productBuilder.priceBySize(priceMap).build());
    }


    private Mono<Product> saveProductAndReturn(final ProductDto productDto, final UUID newProductId) {
        return productRepository.save(
                Product.builder()
                        .productId(newProductId)
                        .name(productDto.getProductName())
                        .type(productDto.getType())
                        .newRecord(true)
                        .build());
    }

    private Mono<ProductResponseDto> savePriceAndReturnResponse(
            final ProductDto productDto,
            final UUID newProductId,
            final ProductResponseDto.ProductResponseDtoBuilder productResponse
    ) {
        return priceRepository.saveAll(mapPricesBySizeToPrices(
                        productDto.getPriceBySize(),
                        newProductId
                ))
                .collectList()
                .map(prices -> productResponse
                        .priceBySize(prices.stream()
                                .collect(Collectors.toMap(Price::getPizzaSize, Price::getValue)))
                        .build());
    }

    private Mono<ProductResponseDto.ProductResponseDtoBuilder> saveInventoryForProductAndReturnBuilder(
            final ProductDto productDto,
            final UUID newProductId,
            final UUID newInventoryId,
            final Product product
    ) {
        return inventoryRepository.save(
                        Inventory.builder()
                                .id(newInventoryId)
                                .productId(newProductId)
                                .availableQuantity(productDto.getInitialInventory())
                                .newRecord(true)
                                .build())
                .map(inventory -> ProductResponseDto.builder()
                        .id(newProductId)
                        .productName(product.getName())
                        .type(product.getType())
                        .inventory(inventory.getAvailableQuantity()));
    }

    private List<Price> mapPricesBySizeToPrices(final Map<PizzaSize, Integer> pricesBySize, final UUID productId) {

        return pricesBySize.entrySet().stream()
                .map(prices -> Price.builder()
                        .id(UUID.randomUUID())
                        .productId(productId)
                        .value(prices.getValue())
                        .pizzaSize(prices.getKey())
                        .newRecord(true)
                        .build())
                .toList();
    }


    private Mono<Product> checkIfProductTypeMatchesForProductId(final ProductOrderDto productDto, final Product product) {
        if (!productDto.getProductType().equals(product.getType())) {
            return Mono.error(new IllegalArgumentException("Product type: " + productDto.getProductType()
                    + " does not exist for product id: " + productDto.getId()));
        }
        return Mono.just(product);
    }
}
