package ru.practicum.ewm.main.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminController {

    @GetMapping("/admin/users")
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return null;
    }

    @PostMapping("/admin/users")
    public UserDto registerUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        return null;
    }

    @DeleteMapping("/admin/users/{userId}")
    public void deleteUser(@PathVariable Long userId) {
    }

    @PostMapping("/admin/categories")
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return null;
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto categoryDto) {
        return null;
    }

    @DeleteMapping("/admin/categories/{catId}")
    public void deleteCategory(@PathVariable Long catId) {
    }

    @PostMapping("/admin/compilations")
    public CompilationDto saveCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        return null;
    }

    @PatchMapping("/admin/compilations/{compId}")
    public CompilationDto updateCompilation(
            @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        return null;
    }

    @DeleteMapping("/admin/compilations/{compId}")
    public void deleteCompilation(@PathVariable Long compId) {
    }

    @GetMapping("/admin/events")
    public List<EventFullDto> getEventsAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return null;
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEventAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateRequest) {
        return null;
    }
}