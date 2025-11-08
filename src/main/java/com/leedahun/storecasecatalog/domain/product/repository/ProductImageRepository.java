package com.leedahun.storecasecatalog.domain.product.repository;

import com.leedahun.storecasecatalog.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
