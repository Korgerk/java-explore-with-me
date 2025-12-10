package ru.practicum.ewm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.service.CompilationService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Public: Подборки событий")
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;

    @Operation(summary = "Получение подборок событий")
    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Запрос списка подборок с фильтром");
        return compilationService.getCompilations(pinned, PageRequest.of(from / size, size));
    }

    @Operation(summary = "Получение подборки событий по его id")
    @GetMapping("/{compId}")
    public CompilationDto getCompilation(
            @PathVariable Long compId
    ) {
        log.info("Запрос подборки по ид={}", compId);
        return compilationService.getCompilation(compId);
    }

}