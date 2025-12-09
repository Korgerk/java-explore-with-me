package ru.practicum.explorewithme.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.dto.CompilationDto;
import ru.practicum.explorewithme.dto.EventFullDto;
import ru.practicum.explorewithme.dto.EventShortDto;
import ru.practicum.explorewithme.service.CategoryService;
import ru.practicum.explorewithme.service.CompilationService;
import ru.practicum.explorewithme.service.EventService;
import ru.practicum.explorewithme.service.StatsService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PublicController {
    private final EventService eventService;
    private final CategoryService categoryService;
    private final CompilationService compilationService;
    private final StatsService statsService;

    @GetMapping("/events")
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        log.info("GET /events text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);

        statsService.recordHit(request);
        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("GET /events/{}", id);
        statsService.recordEventView(id, request);
        return eventService.getPublishedEvent(id);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /categories from={}, size={}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("GET /categories/{}", catId);
        return categoryService.getCategory(catId);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /compilations pinned={}, from={}, size={}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.info("GET /compilations/{}", compId);
        return compilationService.getCompilation(compId);
    }
}