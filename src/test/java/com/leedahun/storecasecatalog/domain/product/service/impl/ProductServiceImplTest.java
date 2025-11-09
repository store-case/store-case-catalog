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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceImpl productService;

    private Long sellerId;
    private Long storeId;
    private String expectedApiUrl;

    @BeforeEach
    void setUp() {
        sellerId = 1L;
        storeId = 100L;
        expectedApiUrl = "http://localhost:8081/api/internal/identity/users/" + sellerId;
    }

    @Test
    @DisplayName("getStoreId: 외부 API 호출 성공 시 storeId를 반환한다")
    void getStoreId_Success() {
        // Given
        // RestTemplate이 반환할 응답(HttpResponse) 생성
        HttpResponse mockHttpResponse = new HttpResponse(HttpStatus.OK, "Success", storeId);

        // 응답을 ResponseEntity로 래핑
        ResponseEntity<HttpResponse> mockResponseEntity = new ResponseEntity<>(mockHttpResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(expectedApiUrl, HttpResponse.class)).thenReturn(mockResponseEntity);

        // When
        Long result = productService.getStoreId(sellerId);

        // Then
        assertThat(result).isEqualTo(storeId);
        verify(restTemplate, times(1)).getForEntity(expectedApiUrl, HttpResponse.class);
    }

    @Test
    @DisplayName("getStoreId: 외부 API 응답 body가 null이면 EntityNotFoundException을 던진다")
    void getStoreId_ThrowsEntityNotFound_WhenResponseBodyIsNull() {
        // Given
        // body가 null인 ResponseEntity를 생성
        ResponseEntity<HttpResponse> mockResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        // restTemplate.getForEntity가 호출되면, 위에서 만든 가짜 응답을 반환하도록 설정
        when(restTemplate.getForEntity(expectedApiUrl, HttpResponse.class))
                .thenReturn(mockResponseEntity);

        // When & Then
        assertThatThrownBy(() -> productService.getStoreId(sellerId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Seller's StoreId");

        verify(restTemplate, times(1)).getForEntity(expectedApiUrl, HttpResponse.class);
    }

    @Test
    @DisplayName("getStoreId: 외부 API 응답 data가 null이면 EntityNotFoundException을 던진다")
    void getStoreId_ThrowsEntityNotFound_WhenResponseDataIsNull() {
        // Given
        // data 필드가 null인 HttpResponse를 생성
        HttpResponse mockHttpResponse = new HttpResponse(HttpStatus.OK, "Success", null); // data가 null

        // 가짜 응답을 ResponseEntity로 래핑
        ResponseEntity<HttpResponse> mockResponseEntity = new ResponseEntity<>(mockHttpResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(expectedApiUrl, HttpResponse.class))
                .thenReturn(mockResponseEntity);

        // When & Then
        assertThatThrownBy(() -> productService.getStoreId(sellerId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Seller's StoreId");

        verify(restTemplate, times(1)).getForEntity(expectedApiUrl, HttpResponse.class);
    }

    @Test
    @DisplayName("getStoreId: 외부 API가 400 Bad Request를 반환하면 EntityNotFoundException을 던진다")
    void getStoreId_ThrowsEntityNotFound_OnBadRequest() {
        // Given
        when(restTemplate.getForEntity(expectedApiUrl, HttpResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Seller Not Found"));

        // When & Then
        assertThatThrownBy(() -> productService.getStoreId(sellerId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Seller's StoreId");
    }

    @Test
    @DisplayName("getStoreId: 외부 API 연결 실패 시 ExternalApiException을 던진다")
    void getStoreId_ThrowsExternalApi_OnRestClientException() {
        // Given
        when(restTemplate.getForEntity(expectedApiUrl, HttpResponse.class))
                .thenThrow(new RestClientException("Connection refused"));

        // When & Then
        assertThatThrownBy(() -> productService.getStoreId(sellerId))
                .isInstanceOf(ExternalApiException.class);
    }

    @Test
    @DisplayName("createProduct: 상품 등록 시상품, 옵션, 이미지 연결까지 성공적으로 생성한다")
    void createProduct_Success() {
        // Given
        Long categoryId = 10L;
        List<Long> imageIds = List.of(1L, 2L);
        ProductCreateRequestDto requestDto = createMockRequestDto(categoryId, imageIds);

        Category mockCategory = Category.builder().id(categoryId).name("Tops").build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product productToSave = invocation.getArgument(0);
            return productToSave;
        });

        // When
        productService.createProduct(requestDto, storeId);

        // Then
        verify(categoryRepository, times(1)).findById(categoryId);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(1)).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getStoreId()).isEqualTo(storeId);
        assertThat(savedProduct.getName()).isEqualTo(requestDto.getProductName());
        assertThat(savedProduct.getCategory()).isEqualTo(mockCategory);

        ArgumentCaptor<List<Option>> optionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(optionRepository, times(1)).saveAll(optionsCaptor.capture());
        List<Option> savedOptions = optionsCaptor.getValue();

        assertThat(savedOptions).hasSize(2);
        assertThat(savedOptions.get(0).getName()).isEqualTo("Red");
        assertThat(savedOptions.get(1).getName()).isEqualTo("Blue");

        assertThat(savedOptions.get(0).getProduct()).isEqualTo(savedProduct);
        assertThat(savedOptions.get(1).getProduct()).isEqualTo(savedProduct);

        verify(productImageRepository, times(1)).updateProductIds(eq(savedProduct), eq(imageIds));
    }

    @Test
    @DisplayName("createProduct: 카테고리가 존재하지 않으면 EntityNotFoundException을 던진다")
    void createProduct_ThrowsEntityNotFound_WhenCategoryMissing() {
        // Given
        Long missingCategoryId = 99L;
        ProductCreateRequestDto requestDto = createMockRequestDto(missingCategoryId, List.of(1L));

        // CategoryRepository가 빈 Optional을 반환하도록 설정
        when(categoryRepository.findById(missingCategoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(requestDto, storeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category");

        // 롤백되어야 하므로 다른 Repository들은 전혀 호출되지 않았어야 함
        verify(productRepository, never()).save(any());
        verify(optionRepository, never()).saveAll(any());
        verify(productImageRepository, never()).updateProductIds(any(), any());
    }

    private ProductCreateRequestDto createMockRequestDto(Long categoryId, List<Long> imageIds) {
        OptionCreateRequestDto option1 = OptionCreateRequestDto.builder()
                .optionDetail("Red")
                .price(1000)
                .stock(10)
                .build();
        OptionCreateRequestDto option2 = OptionCreateRequestDto.builder()
                .optionDetail("Blue")
                .price(1000)
                .stock(10)
                .build();

        return ProductCreateRequestDto.builder()
                .productName("Test T-Shirt")
                .description("A nice t-shirt")
                .optionName("Color")
                .price(15000)
                .stock(100)
                .categoryId(categoryId)
                .imageIds(imageIds)
                .options(List.of(option1, option2))
                .build();
    }
}