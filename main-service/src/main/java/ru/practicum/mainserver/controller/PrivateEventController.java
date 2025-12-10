package ru.practicum.mainserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainserver.dto.event.EventFullDto;
import ru.practicum.mainserver.dto.event.EventShortDto;
import ru.practicum.mainserver.dto.event.NewEventDto;
import ru.practicum.mainserver.dto.event.UpdateEventUserRequest;
import ru.practicum.mainserver.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.mainserver.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.mainserver.dto.participation.ParticipationRequestDto;
import ru.practicum.mainserver.service.EventService;
import ru.practicum.mainserver.service.ParticipationRequestService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventController {

    private final EventService eventService;
    private final ParticipationRequestService requestService;

    @GetMapping
    public List<EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {

        log.info("Получение событий пользователя id={}, from={}, size={}", userId, from, size);
        return eventService.getEventsByUser(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Создание события пользователем id={}: {}", userId, newEventDto);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Получение события id={} пользователя id={}", eventId, userId);
        return eventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        log.info("Обновление события id={} пользователем id={}: {}", eventId, userId, updateRequest);
        return eventService.updateEventByUser(userId, eventId, updateRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipationRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Получение запросов на участие в событии id={} пользователя id={}", eventId, userId);
        return requestService.getEventParticipationRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обновление статуса запросов для события id={} пользователем id={}: {}", eventId, userId, updateRequest);
        return requestService.updateRequestStatus(userId, eventId, updateRequest);
    }
}