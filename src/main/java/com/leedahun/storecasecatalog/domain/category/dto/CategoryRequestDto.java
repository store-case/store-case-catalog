package com.leedahun.storecasecatalog.domain.category.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
//@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDto {

    @NotEmpty(message = "카테고리 이름은 비어 있을 수 없습니다.")
    private String name;

}
