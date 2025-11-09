package com.leedahun.storecasecatalog.domain.product.service;

import com.leedahun.storecasecatalog.domain.product.dto.ProductCreateRequestDto;

public interface ProductService {

    Long getStoreId(Long sellerId);

    void createProduct(ProductCreateRequestDto productCreateRequestDto, Long storeId);

}
