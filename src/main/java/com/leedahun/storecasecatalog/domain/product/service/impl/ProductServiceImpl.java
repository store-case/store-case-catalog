package com.leedahun.storecasecatalog.domain.product.service.impl;

import com.leedahun.storecasecatalog.common.error.exception.EntityNotFoundException;
import com.leedahun.storecasecatalog.common.error.exception.ExternalApiException;
import com.leedahun.storecasecatalog.common.response.HttpResponse;
import com.leedahun.storecasecatalog.domain.category.entity.Category;
import com.leedahun.storecasecatalog.domain.category.repository.CategoryRepository;
import com.leedahun.storecasecatalog.domain.option.entity.Option;
import com.leedahun.storecasecatalog.domain.option.repository.OptionRepository;
import com.leedahun.storecasecatalog.domain.product.dto.ProductCreateRequestDto;
import com.leedahun.storecasecatalog.domain.product.entity.Product;
import com.leedahun.storecasecatalog.domain.product.repository.ProductImageRepository;
import com.leedahun.storecasecatalog.domain.product.repository.ProductRepository;
import com.leedahun.storecasecatalog.domain.product.service.ProductService;
import java.util.List;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    private final WebClient webClient;

    public ProductServiceImpl(ProductRepository productRepository,
                              OptionRepository optionRepository,
                              CategoryRepository categoryRepository,
                              ProductImageRepository productImageRepository,
                              @LoadBalanced WebClient.Builder webClientBuilder) {

        this.productRepository = productRepository;
        this.optionRepository = optionRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;

        this.webClient = webClientBuilder
                .baseUrl("http://identity-service")
                .build();
    }

    @Override
    public Long getStoreId(Long sellerId) {
        final String PATH = "/api/internal/identity/users/{sellerId}";

        try {
            Mono<HttpResponse> responseMono = webClient.get()
                    .uri(PATH, sellerId)
                    .retrieve()
                    .bodyToMono(HttpResponse.class);

            HttpResponse response = responseMono.block();
            if (response == null || response.getData() == null) {
                throw new EntityNotFoundException("Seller's StoreId", sellerId);
            }

            return ((Number) response.getData()).longValue();
        } catch (WebClientResponseException e) {
            throw new EntityNotFoundException("Seller's StoreId", sellerId);
        } catch (WebClientException e) {
            throw new ExternalApiException();
        }
    }

    @Override
    @Transactional
    public void createProduct(ProductCreateRequestDto productCreateRequestDto, Long storeId) {
        // 카테고리 조회
        Category category = categoryRepository.findById(productCreateRequestDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category", productCreateRequestDto.getCategoryId()));

        // 상품 등록
        Product product = Product.builder()
                .storeId(storeId)
                .name(productCreateRequestDto.getProductName())
                .description(productCreateRequestDto.getDescription())
                .optionName(productCreateRequestDto.getOptionName())
                .price(productCreateRequestDto.getPrice())
                .stock(productCreateRequestDto.getStock())
                .category(category)
                .build();
        productRepository.save(product);

        // 옵션 생성
        List<Option> options = productCreateRequestDto.getOptions().stream()
                .map(optionRequest -> Option.builder()
                        .product(product)
                        .price(optionRequest.getPrice())
                        .stock(optionRequest.getStock())
                        .name(optionRequest.getOptionDetail())
                        .build())
                .toList();
        optionRepository.saveAll(options);

        // 이미지 연결
        productImageRepository.updateProductIds(product, productCreateRequestDto.getImageIds());
    }
}
