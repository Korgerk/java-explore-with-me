package ru.practicum.ewm.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.*;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.exception.ValidationException;
import ru.practicum.ewm.main.mapper.EventMapper;
import ru.practicum.ewm.main.model.*;
import ru.practicum.ewm.main.repository.*;
import ru.practicum.ewm.main.stats.StatsClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationService locationService;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;

    // === Публичные методы ===

    public List<EventShortDto> getPublicEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            int from,
            int size,
            HttpServletRequest request) {

        // Логируем в статистику
        statsClient.hit(request);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("rangeEnd must not be before rangeStart");
        }

        List<Event> events = eventRepository.findPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size);

        // Получаем просмотры для всех событий
        Map<Long, Long> views = statsClient.getViews(events.stream().map(Event::getId).collect(Collectors.toList()));

        // Получаем confirmedRequests
        Map<Long, Long> confirmed;
        if (!events.isEmpty()) {
            List<ParticipationRequest> confirmedRequests = requestRepository.findConfirmedByEventIds(
                    events.stream().map(Event::getId).collect(Collectors.toList()));
            confirmed = confirmedRequests.stream()
                    .collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));
        } else {
            confirmed = new HashMap<>();
        }

        return events.stream()
                .map(e -> EventMapper.toShortDto(e, views.getOrDefault(e.getId(), 0L), confirmed.getOrDefault(e.getId(), 0L)))
                .sorted(getEventComparator(sort))
                .collect(Collectors.toList());
    }

    public EventFullDto getPublicEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + id + " was not found");
        }

        statsClient.hit(request);

        Long views = statsClient.getViews(List.of(id)).getOrDefault(id, 0L);
        Long confirmed = requestRepository.countConfirmedByEventId(id);

        return EventMapper.toFullDto(event, views, confirmed);
    }

    // === Приватные методы ===

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        validateEventDate(newEventDto.getEventDate());

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));
        LocationEntity location = locationService.getOrCreate(newEventDto.getLocation());

        Event event = EventMapper.toEntity(newEventDto, initiator, category, location);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved, 0L, 0L);
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        List<Event> events = eventRepository.findByInitiatorId(userId, from, size);
        Map<Long, Long> confirmed;
        if (!events.isEmpty()) {
            List<ParticipationRequest> confirmedRequests = requestRepository.findConfirmedByEventIds(
                    events.stream().map(Event::getId).collect(Collectors.toList()));
            confirmed = confirmedRequests.stream()
                    .collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));
        } else {
            confirmed = new HashMap<>();
        }

        return events.stream()
                .map(e -> EventMapper.toShortDto(e, 0L, confirmed.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        Long confirmed = requestRepository.countConfirmedByEventId(eventId);
        return EventMapper.toFullDto(event, 0L, confirmed);
    }

    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest update) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getState().equals(EventState.PENDING) && !event.getState().equals(EventState.CANCELED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (update.getStateAction() != null) {
            if (update.getStateAction() == StateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (update.getStateAction() == StateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        updateEventFields(event, update);
        Event updated = eventRepository.save(event);
        Long confirmed = requestRepository.countConfirmedByEventId(eventId);
        return EventMapper.toFullDto(updated, 0L, confirmed);
    }

    // === Админ методы ===

    public List<EventFullDto> getAdminEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size) {

        List<EventState> eventStates = states == null ? null :
                states.stream().map(EventState::valueOf).collect(Collectors.toList());

        List<Event> events = eventRepository.findAdminEvents(users, eventStates, categories, rangeStart, rangeEnd, from, size);

        Map<Long, Long> confirmed;
        if (!events.isEmpty()) {
            List<ParticipationRequest> confirmedRequests = requestRepository.findConfirmedByEventIds(
                    events.stream().map(Event::getId).collect(Collectors.toList()));
            confirmed = confirmedRequests.stream()
                    .collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));
        } else {
            confirmed = new HashMap<>();
        }

        return events.stream()
                .map(e -> EventMapper.toFullDto(e, 0L, confirmed.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest update) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (update.getStateAction() != null) {
            if (update.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Event date must be at least 1 hour after publication");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (update.getStateAction() == AdminStateAction.REJECT_EVENT) {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ConflictException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        updateEventFields(event, update);
        Event updated = eventRepository.save(event);
        Long confirmed = requestRepository.countConfirmedByEventId(eventId);
        return EventMapper.toFullDto(updated, 0L, confirmed);
    }

    // === Вспомогательные методы ===

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours after creation");
        }
    }

    private void updateEventFields(Event event, Object updateDto) {
        if (updateDto instanceof UpdateEventUserRequest) {
            UpdateEventUserRequest u = (UpdateEventUserRequest) updateDto;
            if (u.getTitle() != null) event.setTitle(u.getTitle());
            if (u.getAnnotation() != null) event.setAnnotation(u.getAnnotation());
            if (u.getDescription() != null) event.setDescription(u.getDescription());
            if (u.getCategory() != null) {
                Category cat = categoryRepository.findById(u.getCategory())
                        .orElseThrow(() -> new NotFoundException("Category not found"));
                event.setCategory(cat);
            }
            if (u.getEventDate() != null) {
                validateEventDate(u.getEventDate());
                event.setEventDate(u.getEventDate());
            }
            if (u.getLocation() != null) {
                LocationEntity loc = locationService.getOrCreate(u.getLocation());
                event.setLocation(loc);
            }
            if (u.getPaid() != null) event.setPaid(u.getPaid());
            if (u.getParticipantLimit() != null) event.setParticipantLimit(u.getParticipantLimit());
            if (u.getRequestModeration() != null) event.setRequestModeration(u.getRequestModeration());
        } else if (updateDto instanceof UpdateEventAdminRequest) {
            UpdateEventAdminRequest u = (UpdateEventAdminRequest) updateDto;
            if (u.getTitle() != null) event.setTitle(u.getTitle());
            if (u.getAnnotation() != null) event.setAnnotation(u.getAnnotation());
            if (u.getDescription() != null) event.setDescription(u.getDescription());
            if (u.getCategory() != null) {
                Category cat = categoryRepository.findById(u.getCategory())
                        .orElseThrow(() -> new NotFoundException("Category not found"));
                event.setCategory(cat);
            }
            if (u.getEventDate() != null) {
                // Для админа: >= 1 час до публикации (проверяется при PUBLISH)
                event.setEventDate(u.getEventDate());
            }
            if (u.getLocation() != null) {
                LocationEntity loc = locationService.getOrCreate(u.getLocation());
                event.setLocation(loc);
            }
            if (u.getPaid() != null) event.setPaid(u.getPaid());
            if (u.getParticipantLimit() != null) event.setParticipantLimit(u.getParticipantLimit());
            if (u.getRequestModeration() != null) event.setRequestModeration(u.getRequestModeration());
        }
    }

    private Comparator<EventShortDto> getEventComparator(String sort) {
        if ("VIEWS".equals(sort)) {
            return Comparator.comparing(EventShortDto::getViews).reversed()
                    .thenComparing(EventShortDto::getEventDate);
        } else {
            return Comparator.comparing(EventShortDto::getEventDate);
        }
    }
}