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
import ru.practicum.statsdto.EndpointHit;
import ru.practicum.statsdto.ViewStats;

import javax.validation.ValidationException;
import ru.practicum.statsclient.StatsClient;
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

    private final LocalDateTime appCreationDate = LocalDateTime.parse("2025-11-30 12:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private final StatsClient statsClient;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    @Value("${app.name}")
    private String appName;

    @Override
    @Transactional
    public EventFullDto addEvent(long userId, EventCreateDto eventCreateDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(String.format("Указанный пользователь ид=%s не найден", userId)));

        Category category = categoryRepository.findById(eventCreateDto.getCategory()).orElseThrow(() -> new ValidationException(String.format("Указан ид=%s несуществующей категории", eventCreateDto.getCategory())));

        Event event = eventMapper.toEvent(eventCreateDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0);

        return eventMapper.toEventFullDto(eventRepository.save(event), null);
    }

    @Override
    public EventFullDto getEvent(long eventId, String uri, String ip) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанное событие ид=%s не найдено", eventId)));

        addHit(uri, ip);
        return eventMapper.toEventFullDto(event, getEventView(eventId));
    }

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size, String uri, String ip) {
        if (rangeStart != null && rangeEnd != null && !rangeStart.isBefore(rangeEnd)) {
            throw new ValidationException("Дата старта фильтра должна быть раньше даты окончания фильтра");
        }

        Pageable pageable;
        if (sort == null) {
            pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        } else if (sort.equalsIgnoreCase("VIEWS")) {
            pageable = PageRequest.of(from / size, size, Sort.by("views").ascending());
        } else if (sort.equalsIgnoreCase("EVENT_DATE")) {
            pageable = PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        } else {
            throw new ValidationException("Указан некорректный вариант сортировки");
        }

        addHit(uri, ip);

        rangeStart = rangeStart == null ? LocalDateTime.now() : rangeStart;

        List<Event> events = eventRepository.findEvents(
                text == null ? null : text.toLowerCase(),
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                EventState.PUBLISHED,
                pageable
        );
        return events.stream().map(eventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEvent(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанный пользователь ид=%s не найден", userId)));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанное событие ид=%s не найдено", eventId)));

        return eventMapper.toEventFullDto(event, getEventView(eventId));
    }

    @Override
    public List<EventShortDto> getUserEvents(long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанный пользователь ид=%s не найден", userId)));

        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        List<EventState> eventStates = null;
        if (states != null) {
            eventStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        List<Event> events = eventRepository.findAdminEvents(users, eventStates, categories, rangeStart, rangeEnd, pageable);

        Map<Long, Long> views = getEventsView(events.stream().map(Event::getId).collect(Collectors.toList()));

        return events
                .stream()
                .map(event -> eventMapper.toEventFullDto(event, views.get(event.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(long userId, long eventId, EventUpdateUserDto eventUpdateUserDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанный пользователь ид=%s не найден", userId)));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанное событие ид=%s не найдено или принадлежит другому пользователю", eventId)));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictDataException("Изменить событие нельзя, т.к. оно опубликовано");
        }

        if (event.getEventDate().minusHours(2L).isBefore(LocalDateTime.now())) {
            throw new ConflictDataException("Событие можно менять не позднее чем за 2 часа до начала");
        }
        log.info("{} views {}", eventId, getEventView(eventId));
        return eventMapper.toEventFullDto(
                eventRepository.save(updateEventByUserData(event, eventUpdateUserDto)),
                getEventView(eventId)
        );
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(long eventId, EventUpdateAdminDto eventUpdateAdminDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Указанное событие ид=%s не найдено", eventId)));

        return eventMapper.toEventFullDto(
                eventRepository.save(updateEventByAdminData(event, eventUpdateAdminDto)),
                getEventView(eventId)
        );
    }


    private Event updateEventByUserData(Event event, EventUpdateUserDto updateData) {

        if (updateData.getCategory() != null) {
            var category = categoryRepository.findById(updateData.getCategory())
                    .orElseThrow(() -> new ValidationException(String.format("Указан ид=%s несуществующей категории", updateData.getCategory())));
            event.setCategory(category);
        }

        var stateAction = updateData.getStateAction();
        if (stateAction != null) {

            if (stateAction == EventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (stateAction == EventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            } else {
                throw new ValidationException("Указан некорректный для данного метода stateAction");
            }
        }

        if (updateData.getEventDate() != null) {
            if (updateData.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата события не может быть в прошлом");
            }
            event.setEventDate(updateData.getEventDate());
        }

        if (updateData.getTitle() != null) {
            event.setTitle(updateData.getTitle());
        }

        if (updateData.getAnnotation() != null) {
            event.setAnnotation(updateData.getAnnotation());
        }

        if (updateData.getDescription() != null) {
            event.setDescription(updateData.getDescription());
        }

        if (updateData.getParticipantLimit() != null) {
            event.setParticipantLimit(updateData.getParticipantLimit());
        }

        if (updateData.getLocation() != null) {
            event.setLocation(Location.builder()
                    .lon(updateData.getLocation().getLon())
                    .lat(updateData.getLocation().getLat())
                    .build()
            );
        }

        if (updateData.getPaid() != null) {
            event.setPaid(updateData.getPaid());
        }

        if (updateData.getRequestModeration() != null) {
            event.setRequestModeration(updateData.getRequestModeration());
        }

        return event;
    }

    private Event updateEventByAdminData(Event event, EventUpdateAdminDto updateData) {

        if (updateData.getCategory() != null) {
            var category = categoryRepository.findById(updateData.getCategory())
                    .orElseThrow(() -> new ValidationException("Указан ид несуществующей категории"));
            event.setCategory(category);
        }

        var state = event.getState();
        var updateStateAction = updateData.getStateAction();
        if (updateStateAction != null) {

            if (updateStateAction == EventStateAction.PUBLISH_EVENT) {
                if (state != EventState.PENDING) {
                    throw new ConflictDataException("Публиковать можно только события ожидающие публикации");
                }
                if (event.getEventDate().minusHours(1L).isBefore(LocalDateTime.now())) {
                    throw new ConflictDataException("Событие можно менять не позднее чем за час до начала");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());

            } else if (updateStateAction == EventStateAction.REJECT_EVENT) {
                if (state == EventState.PUBLISHED) {
                    throw new ConflictDataException("Отменить опубликованное событие нельзя");
                }
                event.setState(EventState.CANCELED);

            } else {
                throw new ValidationException("Указан некорректный для данного метода stateAction");
            }
        }

        if (updateData.getTitle() != null) {
            event.setTitle(updateData.getTitle());
        }

        if (updateData.getAnnotation() != null) {
            event.setAnnotation(updateData.getAnnotation());
        }

        if (updateData.getDescription() != null) {
            event.setDescription(updateData.getDescription());
        }

        if (updateData.getEventDate() != null) {
            if (updateData.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ValidationException("Дата события не может быть в прошлом");
            }
            event.setEventDate(updateData.getEventDate());
        }

        if (updateData.getParticipantLimit() != null) {
            event.setParticipantLimit(updateData.getParticipantLimit());
        }

        if (updateData.getLocation() != null) {
            event.setLocation(Location.builder()
                    .lon(updateData.getLocation().getLon())
                    .lat(updateData.getLocation().getLat())
                    .build()
            );
        }

        if (updateData.getPaid() != null) {
            event.setPaid(updateData.getPaid());
        }

        if (updateData.getRequestModeration() != null) {
            event.setRequestModeration(updateData.getRequestModeration());
        }

        return event;
    }

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

        return stats.isEmpty() ? 0L : stats.get(0).getHits(); // Исправлено с getFirst() на get(0)
    }

    private Map<Long, Long> getEventsView(List<Long> ids) {
        List<String> uris = ids.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

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