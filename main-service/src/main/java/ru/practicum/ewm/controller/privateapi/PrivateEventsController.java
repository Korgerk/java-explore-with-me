package ru.practicum.ewm.controller.privateapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.service.event.EventService;

import java.util.List;

import static ru.practicum.ewm.util.ApiPaths.*;

@RestController
@RequestMapping(PRIVATE_USER_EVENTS)
@RequiredArgsConstructor
@Validated
public class PrivateEventsController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getUserEvents(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getUserEvents(userId, from, size);
    }

    @GetMapping(EVENT_ID)
    public EventFullDto getUserEventById(@PathVariable Long userId,
                                         @PathVariable Long eventId) {
        return eventService.getUserEventById(userId, eventId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @RequestBody @Valid NewEventDto dto) {
        return eventService.createEvent(userId, dto);
    }

    @PatchMapping(EVENT_ID)
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest dto) {
        return eventService.updateUserEvent(userId, eventId, dto);
    }
}
