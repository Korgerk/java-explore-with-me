package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
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

import java.util.*;
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
        List<Compilation> comps = pinned == null
                ? compilationRepository.findAll(PageRequestFactory.from(from, size)).getContent()
                : compilationRepository.findByPinned(pinned, PageRequestFactory.from(from, size)).getContent();

        if (comps.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = comps.stream()
                .flatMap(c -> c.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .toList();

        Map<Long, Long> views = statsFacade.getViewsByEventIds(eventIds);
        Map<Long, Long> confirmed = getConfirmedCounts(eventIds);

        return comps.stream()
                .map(c -> toDto(c, views, confirmed))
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation comp = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));

        List<Long> eventIds = comp.getEvents().stream().map(Event::getId).toList();
        Map<Long, Long> views = statsFacade.getViewsByEventIds(eventIds);
        Map<Long, Long> confirmed = getConfirmedCounts(eventIds);

        return toDto(comp, views, confirmed);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation comp = new Compilation();
        comp.setTitle(dto.getTitle());
        comp.setPinned(Boolean.TRUE.equals(dto.getPinned()));

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllById(dto.getEvents());
            comp.getEvents().addAll(events);
        }

        Compilation saved = compilationRepository.save(comp);

        List<Long> eventIds = saved.getEvents().stream().map(Event::getId).toList();
        Map<Long, Long> views = statsFacade.getViewsByEventIds(eventIds);
        Map<Long, Long> confirmed = getConfirmedCounts(eventIds);

        return toDto(saved, views, confirmed);
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
            comp.getEvents().clear();
            if (!dto.getEvents().isEmpty()) {
                List<Event> events = eventRepository.findAllById(dto.getEvents());
                comp.getEvents().addAll(events);
            }
        }

        Compilation saved = compilationRepository.save(comp);

        List<Long> eventIds = saved.getEvents().stream().map(Event::getId).toList();
        Map<Long, Long> views = statsFacade.getViewsByEventIds(eventIds);
        Map<Long, Long> confirmed = getConfirmedCounts(eventIds);

        return toDto(saved, views, confirmed);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found: " + compId);
        }
        compilationRepository.deleteById(compId);
    }

    private CompilationDto toDto(Compilation comp, Map<Long, Long> views, Map<Long, Long> confirmed) {
        List<Event> events = new ArrayList<>(comp.getEvents());
        List<EventShortDto> shortDtos = events.stream()
                .map(e -> eventMapper.toShortDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());
        return compilationMapper.toDto(comp, shortDtos);
    }

    private Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> result = new HashMap<>();
        for (Long id : eventIds) {
            result.put(id, 0L);
        }
        List<Object[]> rows = requestRepository.countByEventIdsAndStatusGrouped(eventIds, RequestStatus.CONFIRMED);
        for (Object[] row : rows) {
            Long eventId = (Long) row[0];
            Long cnt = (Long) row[1];
            result.put(eventId, cnt);
        }
        return result;
    }
}
