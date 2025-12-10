package ru.practicum.mainserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainserver.dto.category.CategoryDto;
import ru.practicum.mainserver.exception.ConflictException;
import ru.practicum.mainserver.exception.NotFoundException;
import ru.practicum.mainserver.exception.ValidationException;
import ru.practicum.mainserver.mapper.CategoryMapper;
import ru.practicum.mainserver.model.Category;
import ru.practicum.mainserver.repository.CategoryRepository;
import ru.practicum.mainserver.repository.EventRepository;
import ru.practicum.mainserver.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Category createCategory(Category category) {
        log.info("Создание категории: {}", category);

        if (categoryRepository.existsByName(category.getName())) {
            throw new ConflictException("Категория с таким именем уже существует");
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Категория создана с id: {}", savedCategory.getId());
        return savedCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Удаление категории с id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("С категорией связаны события, удаление невозможно");
        }

        categoryRepository.delete(category);
        log.info("Категория с id={} удалена", catId);
    }

    @Override
    @Transactional
    public Category updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Обновление категории с id: {}, данные: {}", catId, categoryDto);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));

        if (categoryDto.getName() != null &&
            !categoryDto.getName().equals(category.getName()) &&
            categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Категория с именем '" + categoryDto.getName() + "' уже существует");
        }

        categoryMapper.updateCategoryFromDto(categoryDto, category);

        Category updatedCategory = categoryRepository.save(category);
        log.info("Категория с id={} обновлена", catId);
        return updatedCategory;
    }

    @Override
    public Category getCategory(Long catId) {
        log.info("Получение категории с id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));

        return category;
    }

    @Override
    public List<Category> getCategories(int from, int size) {
        log.info("Получение категорий с from={}, size={}", from, size);

        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).getContent();
    }
}