package ru.practicum.ewm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.service.CategoryService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Public: Категории")
@RequestMapping("/categories")
public class PublicCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Получение категорий")
    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Получен публичный запрос на список категорий");
        return categoryService.getCategories(PageRequest.of(from / size, size));
    }

    @Operation(summary = "Получение информации о категории по её идентификатору")
    @GetMapping("/{catId}")
    public CategoryDto getCategory(
            @PathVariable int catId
    ) {
        log.info("Получен публичный запрос на категорию с ид={}", catId);
        return categoryService.getCategory(catId);
    }

}