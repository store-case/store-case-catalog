package com.leedahun.storecasecatalog.domain.product.service.impl;

import com.leedahun.storecasecatalog.common.error.exception.FileUploadFailedException;
import com.leedahun.storecasecatalog.common.service.S3UploadService;
import com.leedahun.storecasecatalog.domain.product.dto.ProductImageUploadResponseDto;
import com.leedahun.storecasecatalog.domain.product.entity.ProductImage;
import com.leedahun.storecasecatalog.domain.product.repository.ProductImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceImplTest {

    @Mock
    private S3UploadService s3UploadService;

    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test-image-content".getBytes()
        );
    }

    @Test
    @DisplayName("이미지 업로드 성공 시 ID와 Presigned URL이 포함된 DTO를 반환한다")
    void uploadImage_Success() throws IOException {
        // Given
        String s3Key = "product-images/uuid_test-image.jpg";
        String presignedUrl = "https://s3.presigned.url/for/test-image.jpg";

        // S3 업로드가 성공했다고 가정
        when(s3UploadService.uploadFile(mockFile)).thenReturn(s3Key);

        // Presigned URL 생성이 성공했다고 가정
        when(s3UploadService.getPresignedUrl(s3Key)).thenReturn(presignedUrl);

        // When
        ProductImageUploadResponseDto responseDto = productImageService.uploadImage(mockFile);

        // Then
        // 반환된 DTO 검증
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getImageUrl()).isEqualTo(presignedUrl);

        // productImageRepository.save()에 올바른 인자가 전달되었는지 검증
        ArgumentCaptor<ProductImage> imageCaptor = ArgumentCaptor.forClass(ProductImage.class);
        verify(productImageRepository).save(imageCaptor.capture());

        ProductImage capturedImage = imageCaptor.getValue();
        assertThat(capturedImage.getProduct()).isNull();
        assertThat(capturedImage.getOriginalName()).isEqualTo(mockFile.getOriginalFilename());
        assertThat(capturedImage.getS3Key()).isEqualTo(s3Key);

        // S3 서비스가 올바른 순서로 호출되었는지 검증
        verify(s3UploadService, times(1)).uploadFile(mockFile);
        verify(s3UploadService, times(1)).getPresignedUrl(s3Key);
    }

    @Test
    @DisplayName("S3 파일 업로드 중 IOException 발생 시 FileUploadFailedException을 던진다")
    void uploadImage_ThrowsFileUploadFailedException_OnIOException() throws IOException {
        // Given
        when(s3UploadService.uploadFile(mockFile)).thenThrow(new IOException("S3 connection failed"));

        // When & Then
        assertThatThrownBy(() -> productImageService.uploadImage(mockFile))
                .isInstanceOf(FileUploadFailedException.class);

        verify(productImageRepository, never()).save(any(ProductImage.class));
        verify(s3UploadService, never()).getPresignedUrl(anyString());
    }

}