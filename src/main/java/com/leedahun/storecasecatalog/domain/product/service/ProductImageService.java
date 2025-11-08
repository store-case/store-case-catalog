package com.leedahun.storecasecatalog.domain.product.service;

import com.leedahun.storecasecatalog.domain.product.dto.ProductImageUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImageService {

    ProductImageUploadResponseDto uploadImage(MultipartFile file);

}
