package com.leedahun.storecasecatalog.domain.product.repository;

import com.leedahun.storecasecatalog.domain.product.entity.Product;
import com.leedahun.storecasecatalog.domain.product.entity.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Modifying(clearAutomatically = true)   // 벌크 연산 후 영속성 컨텍스트 초기화
    @Query("UPDATE ProductImage pi SET pi.product = :product WHERE pi.id IN :imageIds")
    void updateProductIds(@Param("product") Product product, @Param("imageIds") List<Long> imageIds);

}
