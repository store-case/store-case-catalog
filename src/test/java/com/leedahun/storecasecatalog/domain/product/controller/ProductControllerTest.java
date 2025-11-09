package com.leedahun.storecasecatalog.domain.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.storecasecatalog.common.error.exception.EntityNotFoundException;
import com.leedahun.storecasecatalog.common.message.SuccessMessage;
import com.leedahun.storecasecatalog.common.response.HttpResponse;
import com.leedahun.storecasecatalog.domain.option.dto.OptionCreateRequestDto;
import com.leedahun.storecasecatalog.domain.product.dto.ProductCreateRequestDto;
import com.leedahun.storecasecatalog.domain.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductCreateRequestDto validRequestDto;
    private String validUserId;
    private Long storeId;

    @BeforeEach
    void setUp() {
        validUserId = "123";
        storeId = 1L;

        validRequestDto = ProductCreateRequestDto.builder()
                .productName("Test T-Shirt")
                .description("A nice t-shirt")
                .optionName("Color")
                .price(15000)
                .stock(100)
                .categoryId(10L)
                .imageIds(List.of(1L, 2L))
                .options(List.of(
                        OptionCreateRequestDto.builder().optionDetail("Red").price(0).stock(10).build()
                ))
                .build();
    }

    @Test
    @DisplayName("상품 생성 성공 시 CREATED(201) 상태와 성공 메시지를 반환한다")
    void createProduct_Success() throws Exception {
        // Given
        given(productService.getStoreId(Long.parseLong(validUserId))).willReturn(storeId);

        doNothing().when(productService).createProduct(any(ProductCreateRequestDto.class), eq(storeId));

        String expectedMessage = SuccessMessage.WRITE_SUCCESS.getMessage();
        HttpResponse expectedResponse = new HttpResponse(
                HttpStatus.CREATED,
                expectedMessage,
                null
        );
        String expectedResponseJson = objectMapper.writeValueAsString(expectedResponse);

        // When
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/catalog/product")
                        .header("X-User-Id", validUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        result.andExpect(status().isCreated())
                .andExpect(content().json(expectedResponseJson))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(productService, times(1)).getStoreId(Long.parseLong(validUserId));
        verify(productService, times(1)).createProduct(any(ProductCreateRequestDto.class), eq(storeId));
    }

    @Test
    @DisplayName("getStoreId에서 EntityNotFoundException 발생 시 404(Not Found)를 반환한다")
    void createProduct_Fail_WhenStoreIdNotFound() throws Exception {
        // Given
        given(productService.getStoreId(Long.parseLong(validUserId)))
                .willThrow(new EntityNotFoundException("Seller's StoreId", Long.parseLong(validUserId)));

        // When
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/catalog/product")
                        .header("X-User-Id", validUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        result.andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(), any());
    }

    @Test
    @DisplayName("유효성 검사(@Valid) 실패 시 400(Bad Request)를 반환한다")
    void createProduct_Fail_WhenValidationFails() throws Exception {
        // Given
        ProductCreateRequestDto invalidRequestDto = ProductCreateRequestDto.builder()
                .productName(null)
                .description("A nice t-shirt")
                .build();

        // When
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/catalog/product")
                        .header("X-User-Id", validUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDto))
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        result.andExpect(status().isBadRequest());

        verify(productService, never()).getStoreId(anyLong());
        verify(productService, never()).createProduct(any(), any());
    }
}