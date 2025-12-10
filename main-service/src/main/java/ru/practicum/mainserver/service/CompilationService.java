package ru.practicum.mainserver.service;

import ru.practicum.mainserver.dto.compilation.CompilationDto;
import ru.practicum.mainserver.dto.compilation.NewCompilationDto;
import ru.practicum.mainserver.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long compId);
}