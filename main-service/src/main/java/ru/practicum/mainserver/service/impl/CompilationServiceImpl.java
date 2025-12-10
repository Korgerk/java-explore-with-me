package ru.practicum.mainserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainserver.dto.compilation.CompilationDto;
import ru.practicum.mainserver.dto.compilation.NewCompilationDto;
import ru.practicum.mainserver.dto.compilation.UpdateCompilationRequest;
import ru.practicum.mainserver.exception.ConflictException;
import ru.practicum.mainserver.exception.NotFoundException;
import ru.practicum.mainserver.exception.ValidationException;
import ru.practicum.mainserver.mapper.CompilationMapper;
import ru.practicum.mainserver.model.Compilation;
import ru.practicum.mainserver.model.Event;
import ru.practicum.mainserver.repository.CompilationRepository;
import ru.practicum.mainserver.repository.EventRepository;
import ru.practicum.mainserver.service.CompilationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание подборки: {}", newCompilationDto);

        // Получение событий для подборки
        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));

            // Проверка, что все события найдены
            if (events.size() != newCompilationDto.getEvents().size()) {
                List<Long> foundIds = events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toList());
                List<Long> missingIds = newCompilationDto.getEvents().stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                throw new NotFoundException("События с id=" + missingIds + " не найдены");
            }
        }

        // Создание подборки
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);
        compilation.setEvents(events);

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка создана с id: {}", savedCompilation.getId());

        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        compilationRepository.delete(compilation);
        log.info("Подборка с id={} удалена", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Обновление подборки с id: {}, данные: {}", compId, updateRequest);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        // Обновление событий, если указаны
        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));

            // Проверка, что все события найдены
            if (events.size() != updateRequest.getEvents().size()) {
                List<Long> foundIds = events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toList());
                List<Long> missingIds = updateRequest.getEvents().stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toList());
                throw new NotFoundException("События с id=" + missingIds + " не найдены");
            }

            compilation.setEvents(events);
        }

        // Обновление полей
        compilationMapper.updateCompilationFromRequest(compilation, updateRequest);

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка с id={} обновлена", compId);

        return compilationMapper.toDto(updatedCompilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Получение подборок: pinned={}, from={}, size={}", pinned, from, size);

        validatePaginationParams(from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        return compilationMapper.toDtoList(compilations);
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        log.info("Получение подборки с id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        return compilationMapper.toDto(compilation);
    }

    private void validatePaginationParams(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }
}