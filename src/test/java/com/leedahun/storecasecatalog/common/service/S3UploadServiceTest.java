package com.leedahun.storecasecatalog.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3UploadServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3UploadService s3UploadService;

    private final String TEST_BUCKET = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3UploadService, "bucket", TEST_BUCKET);  // @Value("${cloud.aws.s3.bucket}") 값을 수동으로 주입
    }

    @Test
    @DisplayName("파일 업로드 성공 시 S3 Key를 반환한다")
    void uploadFile_Success() throws IOException {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "hello.jpg",
                "image/jpeg",
                "test file content".getBytes()
        );

        // s3Client.putObject()가 호출될 때의 가짜 응답 정의
        PutObjectResponse mockResponse = PutObjectResponse.builder().build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(mockResponse);

        // When
        String s3Key = s3UploadService.uploadFile(mockFile);

        // Then
        assertThat(s3Key).isNotNull();
        assertThat(s3Key).startsWith("product-images/");
        assertThat(s3Key).endsWith("_hello.jpg");

        // s3Client.putObject가 올바른 인자(bucket, key, contentType 등)로 호출되었는지 검증
        ArgumentCaptor<PutObjectRequest> putRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        // putObject가 1번 호출되었는지, 호출될 때의 인자를 캡처
        verify(s3Client, times(1)).putObject(putRequestCaptor.capture(), requestBodyCaptor.capture());

        PutObjectRequest capturedRequest = putRequestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo(TEST_BUCKET);
        assertThat(capturedRequest.key()).isEqualTo(s3Key);
        assertThat(capturedRequest.contentType()).isEqualTo("image/jpeg");
        assertThat(capturedRequest.contentLength()).isEqualTo(mockFile.getSize());
    }

    @Test
    @DisplayName("파일 업로드 중 스트림 읽기 실패 시 IOException을 던진다")
    void uploadFile_ThrowsIOException() throws IOException {
        // Given
        // getInputStream()에서 IOException을 발생시키는 가짜 파일 생성
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("error.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(100L);
        when(mockFile.getInputStream()).thenThrow(new IOException("Test IO Exception"));

        // When & Then
        assertThatThrownBy(() -> s3UploadService.uploadFile(mockFile))
                .isInstanceOf(IOException.class)
                .hasMessage("Test IO Exception");

        // putObject(저장)는 아예 호출되지 않아야 한다
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("S3 Key로 Presigned URL 생성에 성공한다")
    void getPresignedUrl_Success() throws MalformedURLException {
        // Given
        String s3Key = "product-images/test-key.jpg";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/product-images/test-key.jpg?presigned-params";

        // s3Presigner.presignGetObject()가 호출될 때의 가짜 응답 정의
        PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(new URL(expectedUrl));

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // When
        String presignedUrl = s3UploadService.getPresignedUrl(s3Key);

        // Then
        assertThat(presignedUrl).isEqualTo(expectedUrl);

        // s3Presigner.presignGetObject()가 올바른 인자로 호출되었는지 검증
        ArgumentCaptor<GetObjectPresignRequest> presignRequestCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(s3Presigner, times(1)).presignGetObject(presignRequestCaptor.capture());

        GetObjectPresignRequest capturedRequest = presignRequestCaptor.getValue();
        GetObjectRequest getObjectRequest = capturedRequest.getObjectRequest();

        assertThat(getObjectRequest.bucket()).isEqualTo(TEST_BUCKET);
        assertThat(getObjectRequest.key()).isEqualTo(s3Key);
        assertThat(capturedRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("S3 Key가 null일 때 Presigned URL 요청 시 null을 반환한다")
    void getPresignedUrl_WhenKeyIsNull() {
        // When
        String presignedUrl = s3UploadService.getPresignedUrl(null);

        // Then
        assertThat(presignedUrl).isNull();

        // Presigner가 호출되지 않았는지 검증
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("S3 Key가 비어있을 때 Presigned URL 요청 시 null을 반환한다")
    void getPresignedUrl_WhenKeyIsEmpty() {
        // When
        String presignedUrl = s3UploadService.getPresignedUrl("");

        // Then
        assertThat(presignedUrl).isNull();

        // Presigner가 호출되지 않았는지 검증
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
    }

}