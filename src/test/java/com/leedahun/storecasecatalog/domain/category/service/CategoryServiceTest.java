package com.leedahun.storecasecatalog.domain.category.service;

import com.leedahun.storecasecatalog.common.error.exception.EntityAlreadyExistsException;
import com.leedahun.storecasecatalog.common.error.exception.EntityNotFoundException;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryRequestDto;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryResponseDto;
import com.leedahun.storecasecatalog.domain.category.entity.Category;
import com.leedahun.storecasecatalog.domain.category.repository.CategoryRepository;
import com.leedahun.storecasecatalog.domain.category.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    private Category category;
    private CategoryRequestDto requestDto;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("테스트 카테고리")
                .build();

        requestDto = CategoryRequestDto.builder()
                        .name("테스트 카테고리")
                        .build();
    }

    @Test
    @DisplayName("카테고리 생성 - 성공")
    void createCategory_shouldSaveAndReturnDto() {
        // given
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // when
        CategoryResponseDto responseDto = categoryService.createCategory(requestDto);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getName()).isEqualTo("테스트 카테고리");

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 - 실패(이미 존재)")
    void createCategory_shouldThrowEntityAlreadyExistsException() {
        // given
        given(categoryRepository.existsByName(anyString())).willReturn(true);

        // when & then
        assertThrows(EntityAlreadyExistsException.class, () -> categoryService.createCategory(requestDto));
    }

    @Test
    @DisplayName("모든 카테고리 조회 - 성공")
    void getAllCategories_success() {
        // given
        Category category2 = Category.builder()
                .id(2L)
                .name("테스트 카테고리2")
                .build();
        List<Category> list = Arrays.asList(category, category2);

        given(categoryRepository.findAll()).willReturn(list);

        // when
        List<CategoryResponseDto> responseList = categoryService.getAllCategories();

        // then
        assertThat(responseList).hasSize(2);
        assertThat(responseList.get(0).getName()).isEqualTo("테스트 카테고리");
    }

    @Test
    @DisplayName("특정 카테고리 조회 - 성공")
    void getCategoryById_success() {
        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        CategoryResponseDto responseDto = categoryService.getCategoryById(categoryId);

        // then
        assertThat(responseDto.getId()).isEqualTo(categoryId);
        assertThat(responseDto.getName()).isEqualTo(category.getName());
    }

    @Test
    @DisplayName("특정 카테고리 조회 - 실패(데이터가 존재하지 않음)")
    void getCategoryById_shouldThrowEntityNotFoundException() {
        // given
        Long notFoundId = 99L;
        given(categoryRepository.findById(notFoundId)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(notFoundId));
    }

    @Test
    @DisplayName("카테고리 수정 - 성공")
    void updateCategory_success() {
        // given
        Long categoryId = 1L;
        CategoryRequestDto updateRequest = CategoryRequestDto.builder()
                .name("수정된 이름")
                .build();

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        CategoryResponseDto responseDto = categoryService.updateCategory(categoryId, updateRequest);

        // then
        assertThat(responseDto.getName()).isEqualTo("수정된 이름");
        assertThat(category.getName()).isEqualTo("수정된 이름");
    }

    @Test
    @DisplayName("카테고리 삭제 - 성공")
    void deleteCategory_success() {
        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(categoryId);

        // then
        verify(categoryRepository).delete(category);
    }
}