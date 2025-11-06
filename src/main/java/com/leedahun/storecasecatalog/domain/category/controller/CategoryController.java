package com.leedahun.storecasecatalog.domain.category.controller;

import static com.leedahun.storecasecatalog.common.message.SuccessMessage.DELETE_SUCCESS;
import static com.leedahun.storecasecatalog.common.message.SuccessMessage.READ_SUCCESS;
import static com.leedahun.storecasecatalog.common.message.SuccessMessage.UPDATE_SUCCESS;
import static com.leedahun.storecasecatalog.common.message.SuccessMessage.WRITE_SUCCESS;

import com.leedahun.storecasecatalog.common.response.HttpResponse;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryRequestDto;
import com.leedahun.storecasecatalog.domain.category.dto.CategoryResponseDto;
import com.leedahun.storecasecatalog.domain.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequestDto requestDto) {
        CategoryResponseDto category = categoryService.createCategory(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HttpResponse(HttpStatus.CREATED, WRITE_SUCCESS.getMessage(), category));
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, READ_SUCCESS.getMessage(), categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, READ_SUCCESS.getMessage(), category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDto requestDto) {
        CategoryResponseDto category = categoryService.updateCategory(id, requestDto);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, UPDATE_SUCCESS.getMessage(), category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok()
                .body(new HttpResponse(HttpStatus.OK, DELETE_SUCCESS.getMessage(), null));
    }

}
