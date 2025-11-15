package com.leedahun.storecasecatalog.domain.product.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadResponseDto {
    private Long imageId;
    private String imageUrl;
}
