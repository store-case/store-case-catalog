package com.leedahun.storecasecatalog.domain.category.service.impl;

import com.leedahun.storecasecatalog.common.error.exception.EntityAlreadyExistsException;
import com.leedahun.storecasecatalog.common.error.exception.EntityNotFoundException;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryRequestDto;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryResponseDto;
import com.leedahun.storecasecatalog.domain.category.entity.Category;
import com.leedahun.storecasecatalog.domain.category.repository.CategoryRepository;
import com.leedahun.storecasecatalog.domain.category.service.CategoryService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto) {
        boolean isCategoryExists = categoryRepository.existsByName(categoryRequestDto.getName());
        if (isCategoryExists) {
            throw new EntityAlreadyExistsException("Category", categoryRequestDto.getName());
        }

        Category category = Category.builder()
                .name(categoryRequestDto.getName())
                .build();
        categoryRepository.save(category);
        return new CategoryResponseDto(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = findCategoryById(id);
        return new CategoryResponseDto(category);
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryRequestDto) {
        Category category = findCategoryById(id);

        category.updateName(categoryRequestDto.getName());

        return new CategoryResponseDto(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 카테고리를 찾을 수 없습니다. ID: ",  id));
    }
}
