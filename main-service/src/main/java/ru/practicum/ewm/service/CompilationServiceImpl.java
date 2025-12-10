package ru.practicum.ewm.service;

import javax.validation.ValidationException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CompilationCreateDto;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.CompilationUpdateDto;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto addCompilation(CompilationCreateDto compilationCreateDto) {
        Set<Event> events = new HashSet<>();

        if (compilationCreateDto.getEvents() != null && !compilationCreateDto.getEvents().isEmpty()) {
            events.addAll(eventRepository.findAllById(compilationCreateDto.getEvents()));
        }

        Compilation compilation = Compilation.builder()
                .title(compilationCreateDto.getTitle())
                .pinned(compilationCreateDto.getPinned())
                .events(events)
                .build();

        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void removeCompilation(long comId) {
        compilationRepository.deleteById(comId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compId, CompilationUpdateDto compilationUpdateDto) {
        if (compilationUpdateDto.getTitle() != null && compilationUpdateDto.getTitle().isBlank()) {
            throw new ValidationException("Наименование подборки не может быть пустым");
        }

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанная подборка ид=%s не найдена", compId)));

        if (compilationUpdateDto.getEvents() != null && !compilationUpdateDto.getEvents().isEmpty()) {
            compilation.setEvents(new HashSet<>(eventRepository.findAllById(compilationUpdateDto.getEvents())));
        }

        if (compilationUpdateDto.getTitle() != null) {
            compilation.setTitle(compilationUpdateDto.getTitle());
        }

        if (compilationUpdateDto.getPinned() != null) {
            compilation.setPinned(compilationUpdateDto.getPinned());
        }

        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанная подборки ид=%s не найдена", compId)));

        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        Page<Compilation> compilations = pinned == null ?
                compilationRepository.findAll(pageable) :
                compilationRepository.findByPinned(pinned, pageable);

        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }
}