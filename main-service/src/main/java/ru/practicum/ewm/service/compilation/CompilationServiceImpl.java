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
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.enums.RequestStatus;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.service.stats.StatsFacade;
import ru.practicum.ewm.util.PageRequestFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsFacade statsFacade;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> comps;
        if (pinned == null) {
            comps = compilationRepository.findAll(PageRequestFactory.from(from, size)).getContent();
        } else {
            comps = compilationRepository.findByPinned(pinned, PageRequestFactory.from(from, size)).getContent();
        }

        if (comps.isEmpty()) {
            return List.of();
        }

        return comps.stream()
                .map(this::toDtoWithEvents)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));
        return toDtoWithEvents(comp);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Compilation title must not be empty.");
        }

        Compilation comp = new Compilation();
        comp.setTitle(dto.getTitle());
        comp.setPinned(Boolean.TRUE.equals(dto.getPinned()));

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            comp.getEvents().clear();
            comp.getEvents().addAll(events);
        }

        Compilation saved = compilationRepository.save(comp);
        return toDtoWithEvents(saved);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));

        if (dto.getTitle() != null) {
            comp.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            comp.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            List<Event> events = dto.getEvents().isEmpty() ? List.of() : eventRepository.findAllById(dto.getEvents());
            comp.getEvents().clear();
            comp.getEvents().addAll(events);
        }

        Compilation saved = compilationRepository.save(comp);
        return toDtoWithEvents(saved);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found: " + compId);
        }
        compilationRepository.deleteById(compId);
    }

    private CompilationDto toDtoWithEvents(Compilation comp) {
        if (comp.getEvents() == null || comp.getEvents().isEmpty()) {
            return compilationMapper.toDto(comp, Collections.emptyList());
        }

        List<Long> ids = comp.getEvents().stream().map(Event::getId).toList();
        Map<Long, Long> views = statsFacade.getViewsByEventIds(ids);

        List<EventShortDto> events = comp.getEvents().stream()
                .map(e -> {
                    long confirmed = requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED);
                    long v = views.getOrDefault(e.getId(), 0L);
                    return eventMapper.toShortDto(e, confirmed, v);
                })
                .toList();

        return compilationMapper.toDto(comp, events);
    }
}
