package ru.practicum.ewm.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toEntity(NewCompilationDto newCompilationDto);

    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toDtoList(List<Compilation> compilations);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "events", ignore = true)
    void updateCompilationFromRequest(@MappingTarget Compilation compilation,
                                      @MappingSource ru.practicum.ewm.dto.compilation.UpdateCompilationRequest updateRequest);

    default Set<Long> mapEventsToIds(Set<Event> events) {
        return events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());
    }
}