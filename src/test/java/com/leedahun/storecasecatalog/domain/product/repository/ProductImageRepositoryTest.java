package com.leedahun.storecasecatalog.domain.product.repository;

import com.leedahun.storecasecatalog.domain.product.entity.Product;
import com.leedahun.storecasecatalog.domain.product.entity.ProductImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductImageRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ProductImageRepository productImageRepository;

    private Product product1;
    private Product product2;
    private ProductImage image1;
    private ProductImage image2;
    private ProductImage image3;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product2 = new Product();

        testEntityManager.persist(product1);
        testEntityManager.persist(product2);

        image1 = ProductImage.builder()
                .s3Key("S3키")
                .build();
        image2 = ProductImage.builder()
                .s3Key("S3키")
                .build();
        image3 = ProductImage.builder()
                .s3Key("S3키")
                .product(product2)
                .build();

        testEntityManager.persist(image1);
        testEntityManager.persist(image2);
        testEntityManager.persist(image3);

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("이미지 ID 목록으로 상품 ID를 일괄 업데이트한다")
    void updateProductIds_Success() {
        // Given
        List<Long> imageIdsToUpdate = List.of(image1.getId(), image2.getId());

        // When
        productImageRepository.updateProductIds(product1, imageIdsToUpdate);

        // Then
        testEntityManager.clear();

        ProductImage foundImage1 = testEntityManager.find(ProductImage.class, image1.getId());
        ProductImage foundImage2 = testEntityManager.find(ProductImage.class, image2.getId());
        ProductImage foundImage3 = testEntityManager.find(ProductImage.class, image3.getId());

        // image1이 product1과 연결되었는지 확인
        assertThat(foundImage1.getProduct()).isNotNull();
        assertThat(foundImage1.getProduct().getId()).isEqualTo(product1.getId());

        // image2가 product1과 연결되었는지 확인
        assertThat(foundImage2.getProduct()).isNotNull();
        assertThat(foundImage2.getProduct().getId()).isEqualTo(product1.getId());

        // image3은 여전히 product2와 연결되어 있는지 확인
        assertThat(foundImage3.getProduct()).isNotNull();
        assertThat(foundImage3.getProduct().getId()).isEqualTo(product2.getId());
        assertThat(foundImage3.getProduct().getId()).isNotEqualTo(product1.getId());
    }
}