package ru.practicum.explorewithme.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.event.Location;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.ValidationException;
import ru.practicum.explorewithme.request.model.RequestStatus;
import ru.practicum.explorewithme.request.repository.RequestRepository;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;
import ru.practicum.explorewithme.statsclient.StatsClient;
import ru.practicum.explorewithme.statsclient.dto.EndpointHitDto;
import ru.practicum.explorewithme.statsclient.dto.ViewStatsDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final RequestRepository requestRepository;

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + newEventDto.getEventDate());
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));

        Event event = EventMapper.toEvent(newEventDto, initiator, category);
        event.setCreatedOn(LocalDateTime.now());
        Event savedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(savedEvent);
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, page);
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(
                        event,
                        requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED),
                        0L
                ))
                .collect(Collectors.toList());
    }

    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        return EventMapper.toEventFullDto(
                event,
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED),
                0L
        );
    }

    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        if (!event.getState().equals(EventState.PENDING) && !event.getState().equals(EventState.CANCELED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        Event updated = EventMapper.partialUpdate(event, updateRequest);

        if (updateRequest.getEventDate() != null && updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + updateRequest.getEventDate());
        }

        if ("CANCEL_REVIEW".equals(updateRequest.getStateAction())) {
            updated.setState(EventState.CANCELED);
        } else if ("SEND_TO_REVIEW".equals(updateRequest.getStateAction())) {
            updated.setState(EventState.PENDING);
        }

        Event saved = eventRepository.save(updated);
        return EventMapper.toEventFullDto(
                saved,
                requestRepository.countByEventIdAndStatus(saved.getId(), RequestStatus.CONFIRMED),
                0L
        );
    }

    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort,
                                               int from, int size, HttpServletRequest request) {

        LocalDateTime now = LocalDateTime.now();
        if (rangeStart == null) {
            rangeStart = now;
        }
        if (rangeStart.isBefore(now)) {
            rangeStart = now;
        }
        if (rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("rangeEnd must be after rangeStart");
        }

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.searchEvents(text, categories, paid, rangeStart, rangeEnd, page);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() == 0 || e.getParticipantLimit() > requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED))
                    .collect(Collectors.toList());
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());
        uris.add(request.getRequestURI());

        statsClient.saveHit(new EndpointHitDto("ewm-main-service", request.getRequestURI(), getClientIp(request), LocalDateTime.now()));

        List<ViewStatsDto> stats = Collections.emptyList();
        if (!uris.isEmpty()) {
            stats = statsClient.getStats(rangeStart, rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(10), uris, false);
        }

        Map<Long, Long> views = stats.stream()
                .filter(st -> st.getUri().startsWith("/events/"))
                .collect(Collectors.toMap(st -> extractEventId(st.getUri()), ViewStatsDto::getHits, Long::sum));

        return events.stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .sorted(getEventComparator(sort))
                .collect(Collectors.toList());
    }

    public EventFullDto getPublicEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + id + " was not found");
        }

        statsClient.saveHit(new EndpointHitDto("ewm-main-service", request.getRequestURI(), getClientIp(request), LocalDateTime.now()));

        List<ViewStatsDto> stats = statsClient.getStats(
                event.getPublishedOn() != null ? event.getPublishedOn() : event.getCreatedOn(),
                LocalDateTime.now().plusYears(10),
                List.of("/events/" + id),
                false
        );

        long views = stats.isEmpty() ? 0L : stats.get(0).getHits();
        long confirmed = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);
        return EventMapper.toEventFullDto(event, confirmed, views);
    }

    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states,
                                                List<Long> categories, LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd, int from, int size) {
        List<EventState> eventStates = states == null ? null : states.stream()
                .map(EventState::valueOf)
                .collect(Collectors.toList());

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findForAdmin(users, eventStates, categories, rangeStart, rangeEnd, page);

        return events.stream()
                .map(e -> EventMapper.toEventFullDto(
                        e,
                        requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED),
                        0L
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Event updated = EventMapper.partialUpdate(event, updateRequest);

        if (updateRequest.getEventDate() != null && updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Event date must be at least one hour after publication");
        }

        if ("PUBLISH_EVENT".equals(updateRequest.getStateAction())) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
            }
            updated.setState(EventState.PUBLISHED);
            updated.setPublishedOn(LocalDateTime.now());
        } else if ("REJECT_EVENT".equals(updateRequest.getStateAction())) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                throw new ConflictException("Cannot reject published event");
            }
            updated.setState(EventState.CANCELED);
        }

        Event saved = eventRepository.save(updated);
        return EventMapper.toEventFullDto(
                saved,
                requestRepository.countByEventIdAndStatus(saved.getId(), RequestStatus.CONFIRMED),
                0L
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Long extractEventId(String uri) {
        try {
            return Long.parseLong(uri.split("/")[2]);
        } catch (Exception e) {
            return 0L;
        }
    }

    private java.util.Comparator<EventShortDto> getEventComparator(String sort) {
        if ("VIEWS".equals(sort)) {
            return Comparator.comparing(EventShortDto::getViews).reversed()
                    .thenComparing(EventShortDto::getEventDate);
        } else {
            return Comparator.comparing(EventShortDto::getEventDate);
        }
    }
}