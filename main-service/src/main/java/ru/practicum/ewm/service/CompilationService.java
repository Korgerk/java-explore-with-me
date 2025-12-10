package ru.practicum.ewm.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.CompilationCreateDto;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.CompilationUpdateDto;

import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(CompilationCreateDto compilationCreateDto);

    void removeCompilation(long comId);

    CompilationDto updateCompilation(long compId, CompilationUpdateDto compilationUpdateDto);

    CompilationDto getCompilation(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable);
}