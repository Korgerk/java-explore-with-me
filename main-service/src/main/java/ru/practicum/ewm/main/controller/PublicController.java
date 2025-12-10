package ru.practicum.ewm.main.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicController {

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return null;
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        return null;
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return null;
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        return null;
    }

    @GetMapping("/events")
    public List<EventShortDto> getEvents(
            HttpServletRequest request,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return null;
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEvent(
            HttpServletRequest request,
            @PathVariable Long id) {
        return null;
    }
}