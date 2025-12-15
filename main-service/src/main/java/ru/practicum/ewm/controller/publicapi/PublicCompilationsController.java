package ru.practicum.ewm.controller.publicapi;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.service.compilation.CompilationService;

import java.util.List;

import static ru.practicum.ewm.util.ApiPaths.*;

@Validated
@RestController
@RequestMapping(PUBLIC_COMPILATIONS)
@RequiredArgsConstructor
public class PublicCompilationsController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping(COMP_ID)
    public CompilationDto getCompilation(@PathVariable Long compId) {
        return compilationService.getCompilationById(compId);
    }
}
