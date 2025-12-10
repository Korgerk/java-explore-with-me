package ru.practicum.ewm.main.mapper;

import ru.practicum.ewm.main.dto.CompilationDto;
import ru.practicum.ewm.main.dto.NewCompilationDto;
import ru.practicum.ewm.main.model.Compilation;
import ru.practicum.ewm.main.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static Compilation toEntity(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .events(new ArrayList<>()) // события добавляются отдельно
                .build();
    }

    public static CompilationDto toDto(Compilation compilation) {
        List<ru.practicum.ewm.main.dto.EventShortDto> eventDtos = compilation.getEvents().stream()
                .map(e -> ru.practicum.ewm.main.mapper.EventMapper.toShortDto(e, 0L, 0L))
                .collect(Collectors.toList()); // ← Java 11: collect(Collectors.toList())

        return new CompilationDto(
                compilation.getId(),
                eventDtos,
                compilation.getPinned(),
                compilation.getTitle()
        );
    }
}