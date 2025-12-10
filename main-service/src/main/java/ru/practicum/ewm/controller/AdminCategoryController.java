package ru.practicum.ewm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDataDto;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.service.CategoryService;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Admin: Категории")
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Добавление новой категории")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(
            @RequestBody @Valid CategoryDataDto categoryDataDto
    ) {
        log.info("Получен запрос от админа на добавление новой категории: {}", categoryDataDto);
        return categoryService.addCategory(categoryDataDto);
    }

    @Operation(summary = "Удаление категории")
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @PathVariable int catId
    ) {
        log.info("Получен запрос от админа на удаление категории с ид={}", catId);
        categoryService.removeCategory(catId);
    }

    @Operation(summary = "Изменение категории")
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(
            @PathVariable int catId,
            @RequestBody @Valid CategoryDataDto categoryDataDto
    ) {
        log.info("Получен запрос от админа на изменение категории с ид={}: {}", catId, categoryDataDto);
        return categoryService.updateCategory(catId, categoryDataDto);
    }

}