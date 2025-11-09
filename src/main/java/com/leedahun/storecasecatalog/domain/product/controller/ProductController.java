package com.leedahun.storecasecatalog.domain.product.controller;

import com.leedahun.storecasecatalog.common.message.SuccessMessage;
import com.leedahun.storecasecatalog.common.response.HttpResponse;
import com.leedahun.storecasecatalog.domain.product.dto.ProductCreateRequestDto;
import com.leedahun.storecasecatalog.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestHeader("X-User-Id") String userId,
                                           @Valid @RequestBody ProductCreateRequestDto productCreateRequestDto) {
        Long storeId = productService.getStoreId(Long.parseLong(userId));
        productService.createProduct(productCreateRequestDto, storeId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpResponse(HttpStatus.CREATED, SuccessMessage.WRITE_SUCCESS.getMessage(), null));
    }

}
