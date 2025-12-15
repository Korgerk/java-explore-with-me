package ru.practicum.ewm.controller.publicapi;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.service.category.CategoryService;

import java.util.List;

import static ru.practicum.ewm.util.ApiPaths.*;

@Validated
@RestController
@RequestMapping(PUBLIC_CATEGORIES)
@RequiredArgsConstructor
public class PublicCategoriesController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping(CAT_ID)
    public CategoryDto getCategory(@PathVariable Long catId) {
        return categoryService.getCategoryById(catId);
    }
}
