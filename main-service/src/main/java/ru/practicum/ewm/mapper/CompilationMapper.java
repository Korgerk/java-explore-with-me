package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.model.Compilation;

@Mapper(
        componentModel = "spring",
        uses = {EventMapper.class}
)
public interface CompilationMapper {

    @Mapping(target = "events", qualifiedByName = "toEventShortDto")
    CompilationDto toCompilationDto(Compilation compilation);
}