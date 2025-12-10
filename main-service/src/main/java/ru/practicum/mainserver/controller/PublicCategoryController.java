package ru.practicum.mainserver.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.dto.category.CategoryDto;
import ru.practicum.mainserver.mapper.CategoryMapper;
import ru.practicum.mainserver.model.Category;
import ru.practicum.mainserver.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicCategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Получение категорий: from={}, size={}", from, size);

        List<Category> categories = categoryService.getCategories(from, size);
        return categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("Получение категории id={}", catId);

        Category category = categoryService.getCategory(catId);
        return categoryMapper.toDto(category);
    }
}