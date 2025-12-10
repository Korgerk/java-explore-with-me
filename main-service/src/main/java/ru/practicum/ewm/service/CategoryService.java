package ru.practicum.ewm.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.CategoryDataDto;
import ru.practicum.ewm.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto addCategory(CategoryDataDto categoryDataDto);

    void removeCategory(int catId);

    CategoryDto updateCategory(int catId, CategoryDataDto categoryDataDto);

    List<CategoryDto> getCategories(Pageable pageable);

    CategoryDto getCategory(int catId);
}