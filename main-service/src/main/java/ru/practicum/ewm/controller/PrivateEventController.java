package ru.practicum.ewm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.RequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Private: События")
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;
    private final RequestService requestService;

    @Operation(summary = "Получение событий, добавленных текущим пользователем")
    @GetMapping
    public List<EventShortDto> getEvents(
            @PathVariable("userId") long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Получен запрос событий пользователя с ид={}", userId);
        return eventService.getUserEvents(userId, PageRequest.of(from / size, size, Sort.by("id").ascending()));
    }

    @Operation(summary = "Добавление нового события")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(
            @PathVariable("userId") long userId,
            @RequestBody @Valid EventCreateDto eventCreateDto
    ) {
        log.info("Получен запрос пользователя с ид={} на добавление события: {}", userId, eventCreateDto);
        return eventService.addEvent(userId, eventCreateDto);
    }

    @Operation(summary = "Получение полной информации о событии добавленном текущим пользователем")
    @GetMapping("/{eventId}")
    public EventFullDto getEvent(
            @PathVariable("userId") long userId,
            @PathVariable("eventId") long eventId
    ) {
        log.info("Получен запрос события ид={} пользователя с ид={}", eventId, userId);
        return eventService.getUserEvent(userId, eventId);
    }

    @Operation(summary = "Изменение события добавленного текущим пользователем")
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable("userId") long userId,
            @PathVariable("eventId") long eventId,
            @RequestBody @Valid EventUpdateUserDto eventUpdateUserDto
    ) {
        log.info("Получен запрос пользователя с ид={} на обновления события {} данными: {}",
                userId, eventId, eventUpdateUserDto);
        return eventService.updateEventByUser(userId, eventId, eventUpdateUserDto);
    }

    @Operation(summary = "Получение информации о запросах на участие в событии текущего пользователя")
    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        log.info("Получен запрос пользователя с ид={} на просмотр запросов участия события {}",
                userId, eventId);
        return requestService.getEventParticipants(userId, eventId);
    }

    @Operation(summary = "Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя")
    @PatchMapping("/{eventId}/requests")
    public RequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody @Valid RequestStatusUpdateDto updateRequestDto
    ) {
        log.info("Получен запрос пользователя с ид={} для события {} на изменение статуса заявок: {}",
                userId, eventId, updateRequestDto);
        return requestService.changeRequestStatus(userId, eventId, updateRequestDto);
    }

}