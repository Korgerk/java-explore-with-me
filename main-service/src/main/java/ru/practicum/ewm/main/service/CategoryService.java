package ru.practicum.ewm.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.CategoryDto;
import ru.practicum.ewm.main.dto.NewCategoryDto;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.mapper.CategoryMapper;
import ru.practicum.ewm.main.model.Category;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toEntity(newCategoryDto);
        Category saved = categoryRepository.save(category);
        return CategoryMapper.toDto(saved);
    }

    public List<CategoryDto> getAll(int from, int size) {
        return categoryRepository.findAll(from, size).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        return CategoryMapper.toDto(category);
    }

    @Transactional
    public CategoryDto update(Long catId, CategoryDto categoryDto) {
        Category existing = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        existing.setName(categoryDto.getName());
        Category updated = categoryRepository.save(existing);
        return CategoryMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long catId) {
        if (eventRepository.findByIdAndInitiatorId(catId, null).isPresent()) {
            // Более точно: нужно проверить, есть ли события с этой категорией
            if (!eventRepository.findAdminEvents(null, null, List.of(catId), null, null, 0, 1).isEmpty()) {
                throw new ConflictException("The category is not empty");
            }
        }
        categoryRepository.deleteById(catId);
    }
}