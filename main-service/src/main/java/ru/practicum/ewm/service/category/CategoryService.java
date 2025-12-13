package ru.practicum.ewm.service.category;

import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Long catId);

    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(Long catId, NewCategoryDto dto);

    void deleteCategory(Long catId);
}
