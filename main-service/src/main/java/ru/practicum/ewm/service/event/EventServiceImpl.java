package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.stats.StatsFacade;
import ru.practicum.ewm.util.PageRequestFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsFacade statsFacade;

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               boolean onlyAvailable,
                                               String sort,
                                               int from,
                                               int size,
                                               HttpServletRequest request) {

        statsFacade.hit(request);

        LocalDateTime start = rangeStart == null ? LocalDateTime.now() : rangeStart;
        LocalDateTime end = rangeEnd;

        if (end != null && end.isBefore(start)) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }

        Specification<Event> spec = Specification.where(withState(EventState.PUBLISHED))
                .and(withText(text))
                .and(withCategories(categories))
                .and(withPaid(paid))
                .and(withRange(start, end));

        Sort dbSort = Sort.by(Sort.Direction.ASC, "eventDate");
        List<Event> events = eventRepository.findAll(spec, PageRequestFactory.from(from, size, dbSort)).getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> views = statsFacade.getViewsByEventIds(ids);
        Map<Long, Long> confirmed = getConfirmedCounts(ids);

        List<Event> filtered = events;
        if (onlyAvailable) {
            filtered = events.stream()
                    .filter(e -> isAvailable(e, confirmed.getOrDefault(e.getId(), 0L)))
                    .collect(Collectors.toList());
        }

        List<EventShortDto> dtos = filtered.stream()
                .map(e -> eventMapper.toShortDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());

        if ("VIEWS".equalsIgnoreCase(sort)) {
            dtos.sort(Comparator.comparingLong(EventShortDto::getViews).reversed());
        } else if ("EVENT_DATE".equalsIgnoreCase(sort) || sort == null) {
            dtos.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        return dtos;
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {
        statsFacade.hit(request);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published: " + eventId));

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = statsFacade.getViewsByEventId(eventId);

        return eventMapper.toFullDto(event, confirmed, views);
    }

    @Override
    public List<EventFullDto> getUserEvents(Long userId, int from, int size) {
        ensureUser(userId);

        List<Event> events = eventRepository.findByInitiatorId(
                        userId,
                        PageRequestFactory.from(from, size, Sort.by("id").ascending())
                )
                .getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> views = statsFacade.getViewsByEventIds(ids);
        Map<Long, Long> confirmed = getConfirmedCounts(ids);

        return events.stream()
                .map(e -> eventMapper.toFullDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User initiator = ensureUser(userId);
        Category category = ensureCategory(dto.getCategory());

        validateNewEvent(dto);

        LocalDateTime now = LocalDateTime.now();

        Event event = eventMapper.toEntity(dto, initiator, category);
        event.setCreatedOn(now);
        event.setState(EventState.PENDING);

        Event saved = eventRepository.save(event);

        return eventMapper.toFullDto(saved, 0L, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        ensureUser(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only initiator can update this event");
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Published event cannot be changed");
        }

        validateUpdate(dto.getTitle(), dto.getAnnotation(), dto.getDescription(), dto.getParticipantLimit());

        applyUpdate(event,
                dto.getTitle(),
                dto.getAnnotation(),
                dto.getDescription(),
                dto.getCategory(),
                dto.getEventDate(),
                dto.getPaid(),
                dto.getParticipantLimit(),
                dto.getRequestModeration(),
                dto.getStateAction(),
                false
        );

        Event saved = eventRepository.save(event);

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = statsFacade.getViewsByEventId(eventId);

        return eventMapper.toFullDto(saved, confirmed, views);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             int from,
                                             int size) {

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }

        Specification<Event> spec = Specification.where(withUsers(users))
                .and(withStates(states))
                .and(withCategories(categories))
                .and(withRange(rangeStart, rangeEnd));

        List<Event> events = eventRepository.findAll(
                        spec,
                        PageRequestFactory.from(from, size, Sort.by("id").ascending())
                )
                .getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> ids = events.stream().map(Event::getId).toList();
        Map<Long, Long> views = statsFacade.getViewsByEventIds(ids);
        Map<Long, Long> confirmed = getConfirmedCounts(ids);

        return events.stream()
                .map(e -> eventMapper.toFullDto(
                        e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        validateUpdate(dto.getTitle(), dto.getAnnotation(), dto.getDescription(), dto.getParticipantLimit());

        applyUpdate(event,
                dto.getTitle(),
                dto.getAnnotation(),
                dto.getDescription(),
                dto.getCategory(),
                dto.getEventDate(),
                dto.getPaid(),
                dto.getParticipantLimit(),
                dto.getRequestModeration(),
                dto.getStateAction(),
                true
        );

        Event saved = eventRepository.save(event);

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long views = statsFacade.getViewsByEventId(eventId);

        return eventMapper.toFullDto(saved, confirmed, views);
    }

    private void validateNewEvent(NewEventDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("title must not be blank");
        }
        if (dto.getAnnotation() == null || dto.getAnnotation().isBlank()) {
            throw new BadRequestException("annotation must not be blank");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new BadRequestException("description must not be blank");
        }
        if (dto.getEventDate() == null) {
            throw new BadRequestException("eventDate must not be null");
        }
        if (!dto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours in the future");
        }
        if (dto.getParticipantLimit() < 0) {
            throw new BadRequestException("participantLimit must not be negative");
        }
    }

    private void validateUpdate(String title, String annotation, String description, Integer participantLimit) {
        if (title != null && title.isBlank()) {
            throw new BadRequestException("title must not be blank");
        }
        if (annotation != null && annotation.isBlank()) {
            throw new BadRequestException("annotation must not be blank");
        }
        if (description != null && description.isBlank()) {
            throw new BadRequestException("description must not be blank");
        }
        if (participantLimit != null && participantLimit < 0) {
            throw new BadRequestException("participantLimit must not be negative");
        }
    }

    private void applyUpdate(Event event,
                             String title,
                             String annotation,
                             String description,
                             Long categoryId,
                             LocalDateTime eventDate,
                             Boolean paid,
                             Integer participantLimit,
                             Boolean requestModeration,
                             String stateAction,
                             boolean admin) {

        if (title != null) {
            event.setTitle(title);
        }
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (categoryId != null) {
            event.setCategory(ensureCategory(categoryId));
        }
        if (eventDate != null) {
            if (!eventDate.isAfter(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Event date must be at least 2 hours in the future");
            }
            event.setEventDate(eventDate);
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }

        if (stateAction != null) {
            if (!admin) {
                EventStateActionUser action;
                try {
                    action = EventStateActionUser.valueOf(stateAction);
                } catch (IllegalArgumentException ex) {
                    throw new ConflictException("Unknown stateAction: " + stateAction);
                }
                if (action == EventStateActionUser.SEND_TO_REVIEW) {
                    event.setState(EventState.PENDING);
                } else if (action == EventStateActionUser.CANCEL_REVIEW) {
                    event.setState(EventState.CANCELED);
                }
            } else {
                EventStateActionAdmin action;
                try {
                    action = EventStateActionAdmin.valueOf(stateAction);
                } catch (IllegalArgumentException ex) {
                    throw new ConflictException("Unknown stateAction: " + stateAction);
                }

                if (action == EventStateActionAdmin.PUBLISH_EVENT) {
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Only PENDING event can be published");
                    }
                    if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Event date must be at least 1 hour in the future to publish");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else if (action == EventStateActionAdmin.REJECT_EVENT) {
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Published event cannot be rejected");
                    }
                    event.setState(EventState.CANCELED);
                }
            }
        }
    }

    private boolean isAvailable(Event event, long confirmed) {
        int limit = event.getParticipantLimit();
        return limit == 0 || confirmed < limit;
    }

    private User ensureUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Category ensureCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
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

    private Specification<Event> withState(EventState state) {
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }

    private Specification<Event> withText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String pattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("annotation")), pattern),
                cb.like(cb.lower(root.get("description")), pattern),
                cb.like(cb.lower(root.get("title")), pattern)
        );
    }

    private Specification<Event> withCategories(List<Long> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("category").get("id").in(categories);
    }

    private Specification<Event> withPaid(Boolean paid) {
        if (paid == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    private Specification<Event> withRange(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return null;
        }
        if (start != null && end != null) {
            return (root, query, cb) -> cb.between(root.get("eventDate"), start, end);
        }
        if (start != null) {
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), start);
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    private Specification<Event> withUsers(List<Long> users) {
        if (users == null || users.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("initiator").get("id").in(users);
    }

    private Specification<Event> withStates(List<String> states) {
        if (states == null || states.isEmpty()) {
            return null;
        }
        Set<EventState> parsed = new HashSet<>();
        for (String s : states) {
            try {
                parsed.add(EventState.valueOf(s));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Unknown state: " + s);
            }
        }
        return (root, query, cb) -> root.get("state").in(parsed);
    }
}
