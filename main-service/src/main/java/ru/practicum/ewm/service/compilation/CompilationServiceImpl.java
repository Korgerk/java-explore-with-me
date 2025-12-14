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

import java.util.HashMap;
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
        List<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(PageRequestFactory.from(from, size)).getContent()
                : compilationRepository.findByPinned(pinned, PageRequestFactory.from(from, size)).getContent();

        if (compilations.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .toList();

        Map<Long, Long> views = statsFacade.getViewsByEventIds(eventIds);
        Map<Long, Long> confirmed = getConfirmedCounts(eventIds);

        return compilations.stream()
                .map(c -> toDto(c, views, confirmed))
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));

        List<Long> eventIds = compilation.getEvents().stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> views = statsFacade.getViewsByEventIds(eventIds);
        Map<Long, Long> confirmed = getConfirmedCounts(eventIds);

        return toDto(compilation, views, confirmed);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.isPinned());

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            compilation.getEvents().addAll(eventRepository.findAllById(dto.getEvents()));
        }

        Compilation saved = compilationRepository.save(compilation);

        return toDto(saved, Map.of(), Map.of());
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found: " + compId));

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            compilation.getEvents().clear();
            compilation.getEvents().addAll(eventRepository.findAllById(dto.getEvents()));
        }

        Compilation saved = compilationRepository.save(compilation);

        return toDto(saved, Map.of(), Map.of());
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found: " + compId);
        }
        compilationRepository.deleteById(compId);
    }

    private CompilationDto toDto(Compilation compilation,
                                 Map<Long, Long> views,
                                 Map<Long, Long> confirmed) {

        List<EventShortDto> events = compilation.getEvents().stream()
                .map(e -> eventMapper.toShortDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());

        return compilationMapper.toDto(compilation, events);
    }

    private Map<Long, Long> getConfirmedCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>();
        for (Long id : eventIds) {
            result.put(id, 0L);
        }

        List<Object[]> rows =
                requestRepository.countByEventIdsAndStatusGrouped(eventIds, RequestStatus.CONFIRMED);

        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }

        return result;
    }
}
