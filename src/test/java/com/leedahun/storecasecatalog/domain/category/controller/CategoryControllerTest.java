package com.leedahun.storecasecatalog.domain.category.controller;

import com.leedahun.storecasecatalog.common.message.SuccessMessage;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryRequestDto;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryResponseDto;
import com.leedahun.storecasecatalog.domain.category.entity.Category;
import com.leedahun.storecasecatalog.domain.category.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private CategoryRequestDto requestDto;
    private CategoryResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new CategoryRequestDto();
        requestDto.setName("테스트 카테고리");

        Category category = Category.builder()
                .id(1L)
                .name("test category")
                .build();
        responseDto = new CategoryResponseDto(category);
    }

    @Test
    @DisplayName("카테고리 생성 - 성공")
    void createCategory_success() throws Exception {
        // given
        given(categoryService.createCategory(any(CategoryRequestDto.class)))
                .willReturn(responseDto);

        // when
        ResultActions actions = mockMvc.perform(post("/api/category")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.WRITE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.data.name").value(responseDto.getName()));
    }

    @Test
    @DisplayName("모든 카테고리 조회 - 성공")
    void getAllCategories_success() throws Exception {
        // given
        Category category2 = Category.builder()
                .id(2L)
                .name("test category2")
                .build();
        List<CategoryResponseDto> list = Arrays.asList(
                responseDto,
                new CategoryResponseDto(category2)
        );
        given(categoryService.getAllCategories()).willReturn(list);

        // when
        ResultActions actions = mockMvc.perform(get("/api/category"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value(responseDto.getName()));
    }

    @Test
    @DisplayName("특정 카테고리 조회 - 성공")
    void getCategoryById_success() throws Exception {
        // given
        Long categoryId = 1L;
        given(categoryService.getCategoryById(categoryId)).willReturn(responseDto);

        // when
        ResultActions actions = mockMvc.perform(get("/api/category/{id}", categoryId));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.READ_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.name").value(responseDto.getName()));
    }

    @Test
    @DisplayName("카테고리 수정 - 성공")
    void updateCategory_success() throws Exception {
        // given
        Long categoryId = 1L;
        CategoryRequestDto updateRequest = new CategoryRequestDto();
        updateRequest.setName("수정된 카테고리");

        Category updatedCategory = Category.builder()
                .id(categoryId)
                .name(updateRequest.getName())
                .build();
        CategoryResponseDto updatedResponse = new CategoryResponseDto(updatedCategory);

        given(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDto.class)))
                .willReturn(updatedResponse);

        // when
        ResultActions actions = mockMvc.perform(put("/api/category/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.UPDATE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.name").value("수정된 카테고리"));
    }

    @Test
    @DisplayName("카테고리 삭제 - 성공")
    void deleteCategory_success() throws Exception {
        // given
        Long categoryId = 1L;
        doNothing().when(categoryService).deleteCategory(categoryId);

        // when
        ResultActions actions = mockMvc.perform(delete("/api/category/{id}", categoryId));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value(SuccessMessage.DELETE_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}