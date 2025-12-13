package ru.practicum.ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<EventFullDto> searchEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort,
                                        int from, int size, HttpServletRequest request);

    EventFullDto getEventPublic(Long eventId, HttpServletRequest request);

    Event getEventById(Long eventId);
}