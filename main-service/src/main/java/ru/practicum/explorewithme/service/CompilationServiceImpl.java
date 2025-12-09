package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.CompilationDto;
import ru.practicum.explorewithme.dto.CompilationRequestDto;
import ru.practicum.explorewithme.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.ValidationException;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.model.Compilation;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.repository.CompilationRepository;
import ru.practicum.explorewithme.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final StatsService statsService;

    @Override
    @Transactional
    public CompilationDto createCompilation(CompilationRequestDto compilationDto) {
        List<Event> events = null;
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            events = eventRepository.findByIdIn(compilationDto.getEvents());
        }

        Compilation compilation = compilationMapper.toCompilation(compilationDto);
        compilation.setEvents(events);

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Created compilation with id={}", savedCompilation.getId());

        return compilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " not found"));

        compilationRepository.delete(compilation);
        log.info("Deleted compilation with id={}", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " not found"));

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.getEvents() != null) {
            List<Event> events = eventRepository.findByIdIn(updateRequest.getEvents());
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Updated compilation with id={}", compId);

        return compilationMapper.toCompilationDto(updatedCompilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Invalid pagination parameters");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }

        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .peek(this::enrichCompilationWithStats)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " not found"));

        CompilationDto dto = compilationMapper.toCompilationDto(compilation);
        enrichCompilationWithStats(dto);

        return dto;
    }

    private void enrichCompilationWithStats(CompilationDto compilationDto) {
        if (compilationDto.getEvents() != null) {
            compilationDto.getEvents().forEach(event -> {
                event.setViews(statsService.getViewsForEvent(event.getId()));
            });
        }
    }
}