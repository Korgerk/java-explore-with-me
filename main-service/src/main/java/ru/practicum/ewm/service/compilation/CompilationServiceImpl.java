package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.util.PageRequestFactory;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        if (pinned == null) {
            return compilationRepository.findAll(PageRequestFactory.from(from, size))
                    .map(c -> compilationMapper.toDto(c, Collections.emptyList()))
                    .getContent();
        }
        return compilationRepository.findByPinned(pinned, PageRequestFactory.from(from, size))
                .map(c -> compilationMapper.toDto(c, Collections.emptyList()))
                .getContent();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));
        return compilationMapper.toDto(comp, Collections.emptyList());
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Compilation title must not be empty.");
        }

        Compilation comp = new Compilation();
        comp.setTitle(dto.getTitle());
        comp.setPinned(dto.isPinned());

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            comp.getEvents().addAll(events);
        }

        Compilation saved = compilationRepository.save(comp);
        return compilationMapper.toDto(saved, Collections.emptyList());
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        if (dto.getTitle() != null && dto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Compilation title must not be empty.");
        }

        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));

        if (dto.getTitle() != null) {
            comp.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            comp.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            comp.getEvents().clear();
            if (!dto.getEvents().isEmpty()) {
                List<Event> events = eventRepository.findAllById(dto.getEvents());
                comp.getEvents().addAll(events);
            }
        }

        Compilation saved = compilationRepository.save(comp);
        return compilationMapper.toDto(saved, Collections.emptyList());
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found: " + compId);
        }
        compilationRepository.deleteById(compId);
    }
}
