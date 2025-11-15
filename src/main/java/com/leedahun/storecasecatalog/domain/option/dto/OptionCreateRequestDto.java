package com.leedahun.storecasecatalog.domain.option.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class OptionCreateRequestDto {

    @NotBlank(message = "상세 옵션명은 필수입니다.")
    @Size(max = 150, message = "상세 옵션명은 150자를 초과할 수 없습니다.")
    private String optionDetail;

    @Min(value = 0, message = "옵션 가격은 0원 이상이어야 합니다.")
    private int price;

    @Min(value = 0, message = "옵션 재고는 0개 이상이어야 합니다.")
    private int stock;

}
