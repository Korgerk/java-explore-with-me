package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.exception.ConflictDataException;
import ru.practicum.ewm.exception.EntityNotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsdto.ViewStats;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EventServiceImpl implements EventService {

    @Value("${app.name}")
    private String appName;
    private final LocalDateTime appCreationDate = LocalDateTime.parse("2025-11-30 12:00:00",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, EventCreateDto eventCreateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанный пользователь ид=%s не найден", userId)));

        Category category = categoryRepository.findById(eventCreateDto.getCategory())
                .orElseThrow(() -> new ValidationException(String.format("Указан ид=%s несуществующей категории", eventCreateDto.getCategory())));

        Event event = eventMapper.toEvent(eventCreateDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0);

        return eventMapper.toEventFullDto(eventRepository.save(event), null);
    }

    // ... остальные методы остаются аналогичными ...

    private void addHit(String uri, String ip) {
        statsClient.hit(
                EndpointHit.builder()
                        .uri(uri)
                        .app(appName)
                        .ip(ip)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    private long getEventView(long eventId) {
        List<String> uris = List.of("/events/" + eventId);

        List<ViewStats> stats = statsClient.getStats(
                appCreationDate,
                LocalDateTime.now(),
                uris,
                true
        );

        return stats.isEmpty() ? 0L : stats.getFirst().getHits();
    }

    private Map<Long, Long> getEventsView(List<Long> ids) {
        List<String> uris = ids.stream()
                .map(id -> "/events/" + id)
                .toList();

        List<ViewStats> stats = statsClient.getStats(
                appCreationDate,
                LocalDateTime.now(),
                uris,
                false
        );

        if (stats.isEmpty()) {
            return new HashMap<>();
        }

        return stats.stream()
                .map(viewStats -> {
                    String eventIdStr = viewStats.getUri().substring("/events/".length());
                    Long eventId = Long.parseLong(eventIdStr);
                    return Map.entry(eventId, viewStats.getHits());
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}