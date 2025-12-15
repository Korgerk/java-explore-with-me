package ru.practicum.ewm.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.service.compilation.CompilationService;

import static ru.practicum.ewm.util.ApiPaths.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ADMIN_COMPILATIONS)
public class AdminCompilationsController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Valid NewCompilationDto dto) {
        return compilationService.createCompilation(dto);
    }

    @PatchMapping(COMP_ID)
    public CompilationDto update(@PathVariable Long compId, @RequestBody @Valid UpdateCompilationRequest dto) {
        return compilationService.updateCompilation(compId, dto);
    }

    @DeleteMapping(COMP_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
    }
}
