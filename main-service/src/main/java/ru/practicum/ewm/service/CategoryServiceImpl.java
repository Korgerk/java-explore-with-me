package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDataDto;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.exception.ConflictDataException;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addCategory(CategoryDataDto categoryDataDto) {
        if (categoryRepository.findByName(categoryDataDto.getName()).isPresent()) {
            throw new ConflictDataException("Категория с таким именем существует");
        }

        Category category = categoryMapper.toCategory(categoryDataDto);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void removeCategory(int catId) {
        if (!eventRepository.findByCategoryId(catId).isEmpty()) {
            throw new ConflictDataException("Удалять категорию с привязанными событиями низя");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(int catId, CategoryDataDto categoryDataDto) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Категория с указанным ид=%s не найдена", catId))
        );

        var cat = categoryRepository.findByName(categoryDataDto.getName());
        if (cat.isPresent() && cat.get().getId() != catId) {
            throw new ConflictDataException("Категория с таким именем существует");
        }

        category.setName(categoryDataDto.getName());
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(int catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Категория с указанным ид=%s не найдена", catId))
        );

        return categoryMapper.toCategoryDto(category);
    }
}