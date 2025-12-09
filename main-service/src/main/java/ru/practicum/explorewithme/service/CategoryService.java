package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(CategoryDto categoryDto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategory(Long catId);
}