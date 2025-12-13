package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.model.Category;

import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);

    void deleteCategory(Long catId);

    Category updateCategory(Long catId, CategoryDto categoryDto);

    Category getCategory(Long catId);

    List<Category> getCategories(int from, int size);
}