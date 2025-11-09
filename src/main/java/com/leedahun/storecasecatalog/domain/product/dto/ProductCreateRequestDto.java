package com.leedahun.storecasecatalog.domain.product.dto;

import com.leedahun.storecasecatalog.domain.option.dto.OptionCreateRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequestDto {

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 255, message = "상품명은 255자를 초과할 수 없습니다.")
    private String productName;

    @NotBlank(message = "상품 설명은 필수입니다.")
    @Size(max = 2000, message = "상품 설명은 2000자를 초과할 수 없습니다.")
    private String description;

    @Min(value = 0, message = "기본 가격은 0원 이상이어야 합니다.")
    private Integer price;  // 옵션이 있을 경우 null

    @Min(value = 0, message = "기본 재고는 0개 이상이어야 합니다.")
    private Integer stock;  // 옵션이 있을 경우 null

    @NotBlank(message = "옵션 주제명은 필수입니다.")
    @Size(max = 100, message = "옵션 주제명은 100자를 초과할 수 없습니다.")
    private String optionName;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @Builder.Default
    private List<Long> imageIds = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<OptionCreateRequestDto> options = new ArrayList<>();

}
