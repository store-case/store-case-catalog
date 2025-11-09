package com.leedahun.storecasecatalog.domain.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leedahun.storecasecatalog.common.error.exception.FileUploadFailedException;
import com.leedahun.storecasecatalog.common.message.SuccessMessage;
import com.leedahun.storecasecatalog.common.response.HttpResponse;
import com.leedahun.storecasecatalog.domain.product.dto.ProductImageUploadResponseDto;
import com.leedahun.storecasecatalog.domain.product.service.ProductImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductImageUploadController.class)
class ProductImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductImageService productImageService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile mockFile;
    private ProductImageUploadResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        responseDto = ProductImageUploadResponseDto.builder()
                .imageId(1L)
                .imageUrl("https://s3.test.url/image.jpg")
                .build();
    }

    @Test
    @DisplayName("이미지 업로드 성공 시 CREATED(201) 상태와 DTO를 반환한다")
    void uploadImage_Success() throws Exception {
        // Given
        given(productImageService.uploadImage(any(MockMultipartFile.class)))
                .willReturn(responseDto);

        String expectedMessage = SuccessMessage.WRITE_SUCCESS.getMessage();
        HttpResponse expectedResponse = new HttpResponse(
                HttpStatus.CREATED,
                expectedMessage,
                responseDto
        );
        String expectedResponseJson = objectMapper.writeValueAsString(expectedResponse);

        // When
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/catalog/product/image/upload")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        result.andExpect(status().isCreated())
                .andExpect(content().json(expectedResponseJson))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.data.imageId").value(1L))
                .andExpect(jsonPath("$.data.imageUrl").value("https://s3.test.url/image.jpg"));

        verify(productImageService).uploadImage(any(MockMultipartFile.class));
    }

    @Test
    @DisplayName("서비스에서 FileUploadFailedException 발생 시 500(Internal Server Error)을 반환한다")
    void uploadImage_Fail_WhenServiceThrowsException() throws Exception {
        // Given
        given(productImageService.uploadImage(any(MockMultipartFile.class))).willThrow(new FileUploadFailedException());

        // When
        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/catalog/product/image/upload")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then
        result.andExpect(status().isInternalServerError());

        verify(productImageService).uploadImage(any(MockMultipartFile.class));
    }
}