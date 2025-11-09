package com.leedahun.storecasecatalog.domain.product.service.impl;

import com.leedahun.storecasecatalog.common.error.exception.EntityNotFoundException;
import com.leedahun.storecasecatalog.common.error.exception.ExternalApiException;
import com.leedahun.storecasecatalog.common.response.HttpResponse;
import com.leedahun.storecasecatalog.domain.category.entity.Category;
import com.leedahun.storecasecatalog.domain.category.repository.CategoryRepository;
import com.leedahun.storecasecatalog.domain.option.dto.OptionCreateRequestDto;
import com.leedahun.storecasecatalog.domain.option.entity.Option;
import com.leedahun.storecasecatalog.domain.option.repository.OptionRepository;
import com.leedahun.storecasecatalog.domain.product.dto.ProductCreateRequestDto;
import com.leedahun.storecasecatalog.domain.product.entity.Product;
import com.leedahun.storecasecatalog.domain.product.repository.ProductImageRepository;
import com.leedahun.storecasecatalog.domain.product.repository.ProductRepository;
import com.leedahun.storecasecatalog.domain.product.service.ProductService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final RestTemplate restTemplate;

    @Override
    public Long getStoreId(Long sellerId) {
        final String URL = "http://localhost:8081/api/internal/identity/users/" + sellerId;

        Long storeId = null;
        try {
            ResponseEntity<HttpResponse> response = restTemplate.getForEntity(URL, HttpResponse.class);
            if (response.getBody() == null || response.getBody().getData() == null) {
                throw new EntityNotFoundException("Seller's StoreId", sellerId);
            }
            storeId = ((Number) response.getBody().getData()).longValue();
        } catch (HttpClientErrorException e) {
            throw new EntityNotFoundException("Seller's StoreId", sellerId);
        } catch (RestClientException e) {
            throw new ExternalApiException();
        }

        return storeId;
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
        List<Option> options = new ArrayList<>();
        for (OptionCreateRequestDto optionRequest : productCreateRequestDto.getOptions()) {
            options.add(Option.builder()
                    .product(product)
                    .price(optionRequest.getPrice())
                    .stock(optionRequest.getStock())
                    .name(optionRequest.getOptionDetail())
                    .build());
        }
        optionRepository.saveAll(options);

        // 이미지 연결
        productImageRepository.updateProductIds(product, productCreateRequestDto.getImageIds());
    }
}
