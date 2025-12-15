package ru.practicum.ewm.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.service.category.CategoryService;

import java.util.List;

import static ru.practicum.ewm.util.ApiPaths.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ADMIN_CATEGORIES)
public class AdminCategoriesController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto dto) {
        return categoryService.createCategory(dto);
    }

    @PatchMapping(CAT_ID)
    public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid NewCategoryDto dto) {
        return categoryService.updateCategory(catId, dto);
    }

    @DeleteMapping(CAT_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                           @RequestParam(defaultValue = "10") @Positive int size) {
        return categoryService.getCategories(from, size);
    }
}
