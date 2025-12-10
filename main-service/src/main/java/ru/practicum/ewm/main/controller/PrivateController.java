package ru.practicum.ewm.main.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateController {

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return null;
    }

    @PostMapping("/users/{userId}/events")
    public EventFullDto addEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {
        return null;
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return null;
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return null;
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return null;
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest statusUpdate) {
        return null;
    }

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        return null;
    }

    @PostMapping("/users/{userId}/requests")
    public ParticipationRequestDto addParticipationRequest(
            HttpServletRequest request,
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        return null;
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId) {
        return null;
    }
}