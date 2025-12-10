package ru.practicum.ewm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CompilationCreateDto;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.CompilationUpdateDto;
import ru.practicum.ewm.service.CompilationService;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Admin: Подборки событий")
@RequestMapping("/admin/compilations")
public class AdminCompilationController {

    private final CompilationService compilationService;

    @Operation(summary = "Добавление новой подборки (подборка может не содержать событий)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto saveCompilation(
            @RequestBody @Valid CompilationCreateDto compilationCreateDto
    ) {
        log.info("Получен запрос от админа на создание подборки: {}", compilationCreateDto);
        return compilationService.addCompilation(compilationCreateDto);
    }

    @Operation(summary = "Удаление подборки")
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(
            @PathVariable long compId
    ) {
        log.info("Получен запрос от админа на удаление подборки с ид={}", compId);
        compilationService.removeCompilation(compId);
    }

    @Operation(summary = "Обновить информацию о подборке")
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(
            @PathVariable long compId,
            @RequestBody @Valid CompilationUpdateDto compilationUpdateDto
    ) {
        log.info("Получен запрос от админа на изменение подборки с ид={}: {}", compId, compilationUpdateDto);
        return compilationService.updateCompilation(compId, compilationUpdateDto);
    }
}