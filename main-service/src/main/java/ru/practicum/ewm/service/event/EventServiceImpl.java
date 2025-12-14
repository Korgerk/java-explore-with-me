package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.util.PageRequestFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               boolean onlyAvailable, String sort,
                                               int from, int size, HttpServletRequest request) {
        return List.of();
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {
        return null;
    }

    @Override
    public List<EventFullDto> getUserEvents(Long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return eventRepository.findByInitiatorId(userId, PageRequestFactory.from(from, size))
                .map(e -> eventMapper.toFullDto(e, 0L, 0L))
                .getContent();
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        if (event.getInitiator() == null || event.getInitiator().getId() == null || !event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event not found: " + eventId);
        }
        return eventMapper.toFullDto(event, 0L, 0L);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        return null;
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        return null;
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             int from, int size) {
        return List.of();
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest dto) {
        return null;
    }
}
