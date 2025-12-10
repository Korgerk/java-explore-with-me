package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.CategoryDto;
import ru.practicum.ewm.main.dto.NewCategoryDto;
import ru.practicum.ewm.main.model.Category;

public class CategoryMapper {

    public static Category toEntity(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public static CategoryDto toDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}