package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.CompilationDto;
import ru.practicum.explorewithme.dto.CompilationRequestDto;
import ru.practicum.explorewithme.dto.UpdateCompilationRequest;
import java.util.List;

public interface CompilationService {

    CompilationDto createCompilation(CompilationRequestDto compilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilation(Long compId);
}