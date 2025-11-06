package com.leedahun.storecasecatalog.domain.category.service;

import com.leedahun.storecasecatalog.domain.category.dto.CategoryRequestDto;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryResponseDto;
import java.util.List;

public interface CategoryService {

    CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto);

    List<CategoryResponseDto> getAllCategories();

    CategoryResponseDto getCategoryById(Long id);

    CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryRequestDto);

    void deleteCategory(Long id);

}
