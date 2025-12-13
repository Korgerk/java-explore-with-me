package ru.practicum.ewm.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем id={}: {}", userId, newEventDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть не раньше, чем через 2 часа от текущего момента");
        }

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);
        log.info("Событие создано с id: {}", savedEvent.getId());

        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя id={}, from={}, size={}", userId, from, size);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        validatePaginationParams(from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Получение события id={} пользователя id={}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " у пользователя id=" + userId + " не найдено"));

        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Обновление события id={} пользователем id={}: {}", eventId, userId, updateRequest);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " у пользователя id=" + userId + " не найдено"));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Редактировать можно только события в состоянии PENDING или CANCELED");
        }

        if (updateRequest.getEventDate() != null &&
            updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть не раньше, чем через 2 часа от текущего момента");
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + updateRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        eventMapper.updateEventFromUserRequest(updateRequest, event);

        if (updateRequest.getStateAction() != null) {
            StateActionUser stateAction = StateActionUser.valueOf(updateRequest.getStateAction());
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие id={} обновлено пользователем id={}", eventId, userId);

        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public List<EventFullDto> searchEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  int from, int size) {
        log.info("Поиск событий администратором: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        validatePaginationParams(from, size);

        List<EventState> eventStates = null;
        if (states != null && !states.isEmpty()) {
            eventStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findEventsByAdmin(
                users, eventStates, categories, rangeStart, rangeEnd, pageable
        ).getContent();

        return events.stream()
                .map(eventMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Обновление события id={} администратором: {}", eventId, updateRequest);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (updateRequest.getEventDate() != null &&
            updateRequest.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
            throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + updateRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        eventMapper.updateEventFromAdminRequest(updateRequest, event);

        if (updateRequest.getStateAction() != null) {
            StateActionAdmin stateAction = StateActionAdmin.valueOf(updateRequest.getStateAction());
            switch (stateAction) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Событие можно публиковать только в состоянии PENDING");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Событие можно отклонить только если оно еще не опубликовано");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие id={} обновлено администратором", eventId);

        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort,
                                               int from, int size, HttpServletRequest request) {
        log.info("Получение событий публично: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                 "onlyAvailable={}, sort={}, from={}, size={}, ip={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request.getRemoteAddr());

        validatePaginationParams(from, size);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        Sort sorting = Sort.unsorted();
        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE":
                    sorting = Sort.by("eventDate").ascending();
                    break;
                case "VIEWS":
                    sorting = Sort.by("views").descending();
                    break;
            }
        }

        Pageable pageable = PageRequest.of(from / size, size, sorting);

        List<Event> events = eventRepository.findEventsPublic(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable
        ).getContent();

        saveEndpointHit(request);

        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventPublic(Long eventId, HttpServletRequest request) {
        log.info("Получение события id={} публично, ip={}", eventId, request.getRemoteAddr());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не опубликовано");
        }

        saveEndpointHit(request);

        return eventMapper.toFullDto(event);
    }

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private void validatePaginationParams(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }

    private void saveEndpointHit(HttpServletRequest request) {
        EndpointHitDto endpointHit = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            statsClient.addHit(endpointHit);
            log.info("Статистика сохранена для URI: {}", request.getRequestURI());
        } catch (Exception e) {
            log.error("Ошибка при сохранении статистики: {}", e.getMessage());
        }
    }
}