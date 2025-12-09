package ru.practicum.explorewithme.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.service.CategoryService;
import ru.practicum.explorewithme.service.CompilationService;
import ru.practicum.explorewithme.service.EventService;
import ru.practicum.explorewithme.service.UserService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    // Users
    @GetMapping("/users")
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /admin/users ids={}, from={}, size={}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /admin/users {}", userDto);
        return userService.createUser(userDto);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
    }

    // Categories
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("POST /admin/categories {}", categoryDto);
        return categoryService.createCategory(categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto categoryDto) {
        log.info("PATCH /admin/categories/{} {}", catId, categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }

    // Events
    @GetMapping("/events")
    public List<EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /admin/events users={}, states={}, categories={}, rangeStart={}, rangeEnd={}",
                users, states, categories, rangeStart, rangeEnd);
        return eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PutMapping("/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventAdminRequest updateRequest) {
        log.info("PUT /admin/events/{} {}", eventId, updateRequest);
        return eventService.updateEventByAdmin(eventId, updateRequest);
    }

    @PatchMapping("/events/{eventId}/publish")
    public EventFullDto publishEvent(@PathVariable Long eventId) {
        log.info("PATCH /admin/events/{}/publish", eventId);
        return eventService.publishEvent(eventId);
    }

    @PatchMapping("/events/{eventId}/reject")
    public EventFullDto rejectEvent(@PathVariable Long eventId) {
        log.info("PATCH /admin/events/{}/reject", eventId);
        return eventService.rejectEvent(eventId);
    }

    // Compilations
    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody CompilationRequestDto compilationDto) {
        log.info("POST /admin/compilations {}", compilationDto);
        return compilationService.createCompilation(compilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("DELETE /admin/compilations/{}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        log.info("PATCH /admin/compilations/{} {}", compId, updateRequest);
        return compilationService.updateCompilation(compId, updateRequest);
    }
}