package ru.practicum.ewm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.RequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Private: Запросы на участие")
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {

    private final RequestService requestService;

    @Operation(summary = "Получение информации о заявках текущего пользователя на участие в чужих событиях")
    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(
            @PathVariable Long userId
    ) {
        log.info("Получен запрос пользователя ид={} на получение своих запросов", userId);
        return requestService.getUserRequests(userId);
    }

    @Operation(summary = "Добавление запроса от текущего пользователя на участие в событии")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId
    ) {
        log.info("Получен запрос пользователя ид={} на участие в событии ид={}", userId, eventId);
        return requestService.addRequest(userId, eventId);
    }

    @Operation(summary = "Отмена своего запроса на участие в событии")
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) {
        log.info("Получен запрос пользователя ид={} на отмену запроса ид={}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

}