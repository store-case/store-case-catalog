package com.leedahun.storecasecatalog.domain.product.service.impl;

import com.leedahun.storecasecatalog.common.error.exception.FileUploadFailedException;
import com.leedahun.storecasecatalog.common.service.S3UploadService;
import com.leedahun.storecasecatalog.domain.product.dto.ProductImageUploadResponseDto;
import com.leedahun.storecasecatalog.domain.product.entity.ProductImage;
import com.leedahun.storecasecatalog.domain.product.repository.ProductImageRepository;
import com.leedahun.storecasecatalog.domain.product.service.ProductImageService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final S3UploadService s3UploadService;
    private final ProductImageRepository productImageRepository;

    @Override
    public ProductImageUploadResponseDto uploadImage(MultipartFile file) {
        try {
            String s3Key = s3UploadService.uploadFile(file);

            ProductImage productImage = ProductImage.builder()
                    .product(null)
                    .originalName(file.getOriginalFilename())
                    .s3Key(s3Key)
                    .build();
            productImageRepository.save(productImage);

            String preSignedUrl = s3UploadService.getPresignedUrl(s3Key);

            return ProductImageUploadResponseDto.builder()
                    .imageId(productImage.getId())
                    .imageUrl(preSignedUrl)
                    .build();
        } catch (IOException e) {
            throw new FileUploadFailedException();
        }
    }
}
