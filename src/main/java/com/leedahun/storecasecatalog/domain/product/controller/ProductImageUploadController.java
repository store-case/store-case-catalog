package com.leedahun.storecasecatalog.domain.product.controller;

import com.leedahun.storecasecatalog.common.message.SuccessMessage;
import com.leedahun.storecasecatalog.common.response.HttpResponse;
import com.leedahun.storecasecatalog.domain.product.dto.ProductImageUploadResponseDto;
import com.leedahun.storecasecatalog.domain.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog/product/image")
public class ProductImageUploadController {

    private final ProductImageService productImageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        ProductImageUploadResponseDto productImageUploadResponse = productImageService.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpResponse(HttpStatus.CREATED, SuccessMessage.WRITE_SUCCESS.getMessage(), productImageUploadResponse));
    }

}
