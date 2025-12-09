package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.ValidationException;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.model.Category;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.User;
import ru.practicum.explorewithme.model.enums.EventState;
import ru.practicum.explorewithme.model.enums.RequestStatus;
import ru.practicum.explorewithme.model.enums.SortType;
import ru.practicum.explorewithme.model.enums.StateAction;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatsService statsService;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " not found"));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date must be at least 2 hours from now");
        }

        Event event = eventMapper.toEvent(newEventDto);
        event.setCategory(category);
        event.setInitiator(user);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);
        log.info("Created event with id={}", savedEvent.getId());

        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .peek(dto -> dto.setConfirmedRequests(getConfirmedRequests(dto.getId())))
                .peek(dto -> dto.setViews(statsService.getViewsForEvent(dto.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found for user id=" + userId));

        EventFullDto dto = eventMapper.toEventFullDto(event);
        dto.setConfirmedRequests(getConfirmedRequests(eventId));
        dto.setViews(statsService.getViewsForEvent(eventId));

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot update published event");
        }

        if (updateRequest.getEventDate() != null &&
            updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Event date must be at least 2 hours from now");
        }

        updateEventFields(event, updateRequest);

        if (updateRequest.getStateAction() == StateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        } else if (updateRequest.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        Event updatedEvent = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(updatedEvent);
        dto.setConfirmedRequests(getConfirmedRequests(eventId));
        dto.setViews(statsService.getViewsForEvent(eventId));

        return dto;
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<EventState> eventStates = null;

        if (states != null) {
            eventStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);

        List<Event> events = eventRepository.findEventsByAdmin(
                users, eventStates, categories, rangeStart, rangeEnd, pageable);

        return events.stream()
                .map(eventMapper::toEventFullDto)
                .peek(dto -> dto.setConfirmedRequests(getConfirmedRequests(dto.getId())))
                .peek(dto -> dto.setViews(statsService.getViewsForEvent(dto.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == StateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish event that is not in PENDING state");
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Cannot publish event less than 1 hour before event date");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction() == StateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        if (updateRequest.getEventDate() != null &&
            updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Event date must be at least 1 hour from now");
        }

        updateEventFields(event, updateRequest);

        Event updatedEvent = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(updatedEvent);
        dto.setConfirmedRequests(getConfirmedRequests(eventId));
        dto.setViews(statsService.getViewsForEvent(eventId));

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Cannot publish event that is not in PENDING state");
        }

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Cannot publish event less than 1 hour before event date");
        }

        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());

        Event publishedEvent = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(publishedEvent);
        dto.setConfirmedRequests(getConfirmedRequests(eventId));
        dto.setViews(statsService.getViewsForEvent(eventId));

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto rejectEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot reject published event");
        }

        event.setState(EventState.CANCELED);

        Event rejectedEvent = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(rejectedEvent);
        dto.setConfirmedRequests(getConfirmedRequests(eventId));
        dto.setViews(statsService.getViewsForEvent(eventId));

        return dto;
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort,
                                               Integer from, Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Start date must be before end date");
        }

        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);

        Sort sorting = Sort.by(Sort.Direction.ASC, "eventDate");
        if (sort != null && sort.equals(SortType.VIEWS.toString())) {
            sorting = Sort.by(Sort.Direction.DESC, "views");
        }

        Pageable pageable = PageRequest.of(from / size, size, sorting);

        List<Event> events = eventRepository.findPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, pageable);

        List<EventShortDto> result = events.stream()
                .map(eventMapper::toEventShortDto)
                .peek(dto -> dto.setConfirmedRequests(getConfirmedRequests(dto.getId())))
                .peek(dto -> dto.setViews(statsService.getViewsForEvent(dto.getId())))
                .collect(Collectors.toList());

        if (onlyAvailable) {
            result = result.stream()
                    .filter(dto -> dto.getConfirmedRequests() <
                                   eventRepository.findById(dto.getId()).get().getParticipantLimit())
                    .collect(Collectors.toList());
        }

        return result;
    }

    @Override
    public EventFullDto getPublishedEvent(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found or not published"));

        EventFullDto dto = eventMapper.toEventFullDto(event);
        dto.setConfirmedRequests(getConfirmedRequests(eventId));
        dto.setViews(statsService.getViewsForEvent(eventId));

        return dto;
    }

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private void updateEventFields(Event event, Object updateRequest) {
        if (updateRequest instanceof UpdateEventUserRequest) {
            UpdateEventUserRequest request = (UpdateEventUserRequest) updateRequest;
            updateFields(event, request);
        } else if (updateRequest instanceof UpdateEventAdminRequest) {
            UpdateEventAdminRequest request = (UpdateEventAdminRequest) updateRequest;
            updateFields(event, request);
        }
    }

    private void updateFields(Event event, UpdateEventUserRequest request) {
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getLocation() != null) event.setLocation(eventMapper.toLocation(request.getLocation()));
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
    }

    private void updateFields(Event event, UpdateEventAdminRequest request) {
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getLocation() != null) event.setLocation(eventMapper.toLocation(request.getLocation()));
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
    }
}