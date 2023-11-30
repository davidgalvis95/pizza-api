package com.fastspring.pizzaapi.service.product;

import com.fastspring.pizzaapi.dto.order.ProductOrderDto;
import com.fastspring.pizzaapi.dto.product.ProductDto;
import com.fastspring.pizzaapi.dto.product.ProductResponseDto;
import com.fastspring.pizzaapi.model.Inventory;
import com.fastspring.pizzaapi.model.enums.PizzaSize;
import com.fastspring.pizzaapi.model.Price;
import com.fastspring.pizzaapi.model.Product;
import com.fastspring.pizzaapi.repository.InventoryRepository;
import com.fastspring.pizzaapi.repository.PriceRepository;
import com.fastspring.pizzaapi.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
                        .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + productDto.getId())))
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
                        .initialInventory(inventory.getAvailableQuantity()));
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
