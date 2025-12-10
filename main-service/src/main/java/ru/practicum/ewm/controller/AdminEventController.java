package ru.practicum.ewm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventUpdateAdminDto;
import ru.practicum.ewm.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Admin: События")
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @Operation(summary = "Поиск событий")
    @GetMapping
    public List<EventFullDto> getEvents_2(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Получен запрос админа на получение событий");
        return eventService.getAdminEvents(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                PageRequest.of(from / size, size, Sort.by("id").ascending())
        );
    }

    @Operation(summary = "Редактирование данных события и его статуса (отклонение/публикация)")
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent_1(
            @PathVariable("eventId") long eventId,
            @RequestBody @Valid EventUpdateAdminDto updateAdminDto
    ) {
        log.info("Получен запрос админа на обновления события {} данными: {}", eventId, updateAdminDto);
        return eventService.updateEventByAdmin(eventId, updateAdminDto);
    }
}