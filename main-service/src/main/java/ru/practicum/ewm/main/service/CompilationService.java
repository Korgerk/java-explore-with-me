package ru.practicum.ewm.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.NewCompilationDto;
import ru.practicum.ewm.main.dto.UpdateCompilationRequest;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.mapper.CompilationMapper;
import ru.practicum.ewm.main.model.Compilation;
import ru.practicum.ewm.main.model.Event;
import ru.practicum.ewm.main.repository.CompilationRepository;
import ru.practicum.ewm.main.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toEntity(newCompilationDto);
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findByIdIn(newCompilationDto.getEvents());
            compilation.setEvents(events);
        }
        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toDto(saved);
    }

    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        List<Compilation> compilations = compilationRepository.findByPinned(pinned, from, size);
        return compilations.stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        return CompilationMapper.toDto(compilation);
    }

    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest update) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        if (update.getTitle() != null) {
            compilation.setTitle(update.getTitle());
        }
        if (update.getPinned() != null) {
            compilation.setPinned(update.getPinned());
        }
        if (update.getEvents() != null) {
            List<Event> events = eventRepository.findByIdIn(update.getEvents());
            compilation.setEvents(events);
        }

        Compilation updated = compilationRepository.save(compilation);
        return CompilationMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long compId) {
        compilationRepository.deleteById(compId);
    }
}