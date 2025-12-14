package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.enums.EventState;
import ru.practicum.ewm.model.enums.EventStateActionAdmin;
import ru.practicum.ewm.model.enums.EventStateActionUser;
import ru.practicum.ewm.model.enums.RequestStatus;
import ru.practicum.ewm.repository.*;
import ru.practicum.ewm.service.stats.StatsFacade;
import ru.practicum.ewm.util.PageRequestFactory;

import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsFacade statsFacade;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               boolean onlyAvailable, String sort,
                                               int from, int size, HttpServletRequest request) {

        statsFacade.hit(request);

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }

        LocalDateTime start = rangeStart == null ? LocalDateTime.now() : rangeStart;

        var spec = where(EventSpecifications.hasState(EventState.PUBLISHED))
                .and(EventSpecifications.dateAfter(start));

        if (rangeEnd != null) {
            spec = spec.and(EventSpecifications.dateBefore(rangeEnd));
        }
        if (text != null && !text.isBlank()) {
            spec = spec.and(EventSpecifications.textLike(text));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(EventSpecifications.categoryIn(categories));
        }
        if (paid != null) {
            spec = spec.and(EventSpecifications.paidIs(paid));
        }

        List<Event> events;
        boolean sortByViews = sort != null && sort.equalsIgnoreCase("VIEWS");

        if (sortByViews) {
            events = eventRepository.findAll(spec);
        } else {
            events = eventRepository.findAll(spec, PageRequestFactory.from(from, size, Sort.by(Sort.Direction.ASC, "eventDate"))).getContent();
        }

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = getConfirmedByEventIds(ids);
        Map<Long, Long> views = statsFacade.getViewsByEventIds(ids);

        List<Event> filtered = events;
        if (onlyAvailable) {
            filtered = events.stream()
                    .filter(e -> {
                        int limit = e.getParticipantLimit();
                        long c = confirmed.getOrDefault(e.getId(), 0L);
                        return limit == 0 || c < limit;
                    })
                    .toList();
        }

        List<Event> ordered;
        if (sortByViews) {
            ordered = new ArrayList<>(filtered);
            ordered.sort(Comparator.comparingLong((Event e) -> views.getOrDefault(e.getId(), 0L)).reversed());
            int startIdx = Math.min(from, ordered.size());
            int endIdx = Math.min(from + size, ordered.size());
            ordered = ordered.subList(startIdx, endIdx);
        } else {
            ordered = filtered;
        }

        return ordered.stream()
                .map(e -> eventMapper.toShortDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {
        statsFacade.hit(request);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = statsFacade.getViewsByEventId(eventId);

        return eventMapper.toFullDto(event, confirmed, views);
    }

    @Override
    public List<EventFullDto> getUserEvents(Long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));

        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequestFactory.from(from, size)).getContent();
        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = getConfirmedByEventIds(ids);
        Map<Long, Long> views = statsFacade.getViewsByEventIds(ids);

        return events.stream()
                .map(e -> eventMapper.toFullDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (event.getInitiator() == null || event.getInitiator().getId() == null || !event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not found: " + eventId);
        }

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = statsFacade.getViewsByEventId(eventId);

        return eventMapper.toFullDto(event, confirmed, views);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found: " + dto.getCategory()));

        validateEventDateForUser(dto.getEventDate());

        Event event = eventMapper.toEntity(dto, user, category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Event saved = eventRepository.save(event);

        return eventMapper.toFullDto(saved, 0L, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (event.getInitiator() == null || event.getInitiator().getId() == null || !event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not found: " + eventId);
        }

        if (dto.getEventDate() != null && dto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Event date must not be in the past");
        }

        if (dto.getEventDate() != null) {
            validateEventDateForUser(dto.getEventDate());
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            if (dto.getParticipantLimit() < 0) {
                throw new BadRequestException("participantLimit must be >= 0");
            }
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found: " + dto.getCategory()));
            event.setCategory(category);
        }
        if (dto.getStateAction() != null) {
            EventStateActionUser action = parseUserAction(dto.getStateAction());
            if (action == EventStateActionUser.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == EventStateActionUser.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = statsFacade.getViewsByEventId(eventId);

        return eventMapper.toFullDto(saved, confirmed, views);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             int from, int size) {

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }

        var spec = where((org.springframework.data.jpa.domain.Specification<Event>) null);

        if (users != null && !users.isEmpty()) {
            spec = spec.and(EventSpecifications.initiatorIn(users));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(EventSpecifications.categoryIn(categories));
        }
        if (states != null && !states.isEmpty()) {
            List<EventState> parsed = states.stream().map(this::parseEventState).toList();
            spec = spec.and(EventSpecifications.stateIn(parsed));
        }
        if (rangeStart != null) {
            spec = spec.and(EventSpecifications.dateAfter(rangeStart));
        }
        if (rangeEnd != null) {
            spec = spec.and(EventSpecifications.dateBefore(rangeEnd));
        }

        List<Event> events = eventRepository.findAll(spec, PageRequestFactory.from(from, size)).getContent();
        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> confirmed = getConfirmedByEventIds(ids);
        Map<Long, Long> views = statsFacade.getViewsByEventIds(ids);

        return events.stream()
                .map(e -> eventMapper.toFullDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot edit the event because it's already published");
        }

        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Event date must be in the future");
            }
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour in the future");
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            if (dto.getParticipantLimit() < 0) {
                throw new BadRequestException("participantLimit must be >= 0");
            }
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found: " + dto.getCategory()));
            event.setCategory(category);
        }

        if (dto.getStateAction() != null) {
            EventStateActionAdmin action = parseAdminAction(dto.getStateAction());

            if (action == EventStateActionAdmin.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Event date must be at least 1 hour in the future");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (action == EventStateActionAdmin.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject the event because it's already published");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = statsFacade.getViewsByEventId(eventId);

        return eventMapper.toFullDto(saved, confirmed, views);
    }

    private void validateEventDateForUser(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new BadRequestException("eventDate must not be null");
        }
        if (dateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours in the future");
        }
    }

    private EventStateActionUser parseUserAction(String value) {
        try {
            return EventStateActionUser.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown stateAction: " + value);
        }
    }

    private EventStateActionAdmin parseAdminAction(String value) {
        try {
            return EventStateActionAdmin.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown stateAction: " + value);
        }
    }

    private EventState parseEventState(String value) {
        try {
            return EventState.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown state: " + value);
        }
    }

    private Map<Long, Long> getConfirmedByEventIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> result = new HashMap<>();
        for (Long id : eventIds) {
            result.put(id, 0L);
        }
        List<Object[]> rows = requestRepository.countByEventIdsAndStatusGrouped(eventIds, RequestStatus.CONFIRMED);
        for (Object[] row : rows) {
            Long id = (Long) row[0];
            Long cnt = (Long) row[1];
            result.put(id, cnt);
        }
        return result;
    }
}
