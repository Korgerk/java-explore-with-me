package ru.practicum.mainserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.dto.category.CategoryDto;
import ru.practicum.mainserver.dto.category.NewCategoryDto;
import ru.practicum.mainserver.mapper.CategoryMapper;
import ru.practicum.mainserver.model.Category;
import ru.practicum.mainserver.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Создание новой категории: {}", newCategoryDto);
        Category category = categoryMapper.toEntity(newCategoryDto);
        Category savedCategory = categoryService.createCategory(category);
        return categoryMapper.toDto(savedCategory);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Удаление категории с id={}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Обновление категории с id={}: {}", catId, categoryDto);
        Category updatedCategory = categoryService.updateCategory(catId, categoryDto);
        return categoryMapper.toDto(updatedCategory);
    }
}